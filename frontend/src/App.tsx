import React, {useMemo, useState, useEffect, useRef} from 'react'

// ===== Helpers =====
const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
const startOfWeek = (d: Date) => {
  const date = new Date(d)
  const day = (date.getDay() + 6) % 7 // Monday=0
  date.setDate(date.getDate() - day)
  date.setHours(0, 0, 0, 0)
  return date
}
const startOfDay = (d: Date) => { const x = new Date(d); x.setHours(0,0,0,0); return x }
const addDays = (d: Date, days: number) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + days)
const addMinutes = (d: Date, minutes: number) => new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes() + minutes)
const fmtDay = (d: Date) => d.toLocaleDateString(undefined, { weekday: 'short' })
const fmtMD = (d: Date) => `${d.getDate()} ${d.toLocaleDateString(undefined, { month: 'short' })}`
const fmtTime = (d: Date) => `${pad(d.getHours())}:${pad(d.getMinutes())}`
const sameDate = (a: Date, b: Date) => a.getFullYear()===b.getFullYear() && a.getMonth()===b.getMonth() && a.getDate()===b.getDate()

// HH:MM <-> minutes helpers
const minToTime = (min: number) => `${pad(Math.floor(min/60))}:${pad(min%60)}`
const timeToMin = (t: string) => { const [h,m] = t.split(':').map(Number); return (h||0)*60 + (m||0) }

// ===== Grid constants =====
const ROW_MINUTES = 30
const DAY_START = 9 * 60 // 09:00
const DAY_END = 18 * 60 // 18:00
const ROWS = (DAY_END - DAY_START) / ROW_MINUTES

// ===== Models =====
type EventType = { id: string; name: string }

type Booking = {
  id: string
  dayIdx: number // 0..6 (Mon..Sun)
  startMin: number // minutes since midnight
  duration: number // minutes
  createdBy: 'HR1' | 'HR2'
  eventTypeName: string
  status: 'APPROVED' | 'NOT_APPROVED'
  company: string
  hrName: string
  hrEmail: string
}

type FreeSlot = { dayIdx: number; startMin: number }

// ===== Defaults =====
const DEFAULT_EVENT_TYPES: EventType[] = [
  { id: 'screen', name: 'Screening' },
  { id: 'tech',   name: 'Technical' },
  { id: 'hr',     name: 'HR Manager' },
]

// Russia time zones (IANA)
const RUS_TZS = [
  'Europe/Kaliningrad',
  'Europe/Moscow','Europe/Kirov','Europe/Volgograd','Europe/Samara',
  'Asia/Yekaterinburg','Asia/Omsk','Asia/Novosibirsk','Asia/Barnaul','Asia/Tomsk','Asia/Novokuznetsk','Asia/Krasnoyarsk',
  'Asia/Irkutsk','Asia/Chita','Asia/Yakutsk','Asia/Khandyga',
  'Asia/Vladivostok','Asia/Sakhalin','Asia/Ust-Nera','Asia/Magadan','Asia/Srednekolymsk',
  'Asia/Kamchatka','Asia/Anadyr'
] as const

// Use 30‑minute units; bookings are multiples of this
const SLOT_UNIT = 30

// enumerate 30m slot starts in [startMin, endMin)
const enumerateStarts = (startMin: number, endMin: number) => {
  const out: number[] = []
  for (let m = startMin; m + SLOT_UNIT <= endMin; m += SLOT_UNIT) out.push(m)
  return out
}

const overlaps = (aStart: number, aDur: number, bStart: number, bDur: number) =>
  aStart < bStart + bDur && bStart < aStart + aDur

// Only treat as overlapping if they are on the SAME day
const overlapsSameDay = (slotDayIdx: number, slotStart: number, slotDur: number, b: Booking) =>
  slotDayIdx === b.dayIdx && overlaps(slotStart, slotDur, b.startMin, b.duration)

// ===== Tiny runtime tests (dev console) =====
;(function __dev_tests__() {
  console.assert(overlaps(60,30,60,120) === true, 'overlap starts same')
  console.assert(overlaps(600,30,780,120) === false, 'no overlap far apart')
  console.assert(overlaps(600,30,630,30) === false, 'adjacent blocks do not overlap')
  console.assert(Math.max(1, Math.ceil(30/30)) === 1, 'units 30=>1')
  console.assert(Math.max(1, Math.ceil(120/30)) === 4, 'units 120=>4')
  const fake: Booking = { id:'t', dayIdx:1, startMin:600, duration:60, eventTypeName:'X', status:'APPROVED', company:'', hrName:'', hrEmail:'' }
  const fake2: Booking = { id:'t2', dayIdx:1, startMin:630, duration:60, eventTypeName:'X', status:'APPROVED', company:'', hrName:'', hrEmail:'' }
  console.assert(overlapsSameDay(2,600,30,fake) === false, 'free slot on other day must NOT disappear')
  console.assert(overlapsSameDay(1,600,30,fake) === true, 'same-day slot at 10:00 should be hidden')
  console.assert(overlapsSameDay(1,600,30,fake2) === false, 'adjacent same-day slot must NOT disappear');
  console.assert(startOfWeek(new Date()).getDay() === 1, 'startOfWeek should return a Monday (getDay() === 1)');
  // Free slot removal simulation
  const before = [{dayIdx:1,startMin:600},{dayIdx:1,startMin:630},{dayIdx:2,startMin:600}] as FreeSlot[]
  const after = before.filter(s => !(s.dayIdx===1 && s.startMin===600))
  console.assert(after.length === 2 && after.some(s=>s.dayIdx===1&&s.startMin===630) && after.some(s=>s.dayIdx===2&&s.startMin===600), 'remove only targeted free slot')
  // Month roll test for date helper
  const jan31 = new Date(2025,0,31)
  console.assert(addDays(jan31,1).getMonth()===1 && addDays(jan31,1).getDate()===1, 'addDays rolls to next month correctly')
  // enumerateStarts test
  const arr = enumerateStarts(600, 720)
  console.assert(arr.length===4 && arr[0]===600 && arr[3]===690, 'enumerate 10:00..12:00 -> 4 slots (600..690)')
  // time helpers
  console.assert(timeToMin('10:30')===630, 'timeToMin 10:30 -> 630')
  console.assert(minToTime(630)==='10:30', 'minToTime 630 -> 10:30')
  const arr2 = enumerateStarts(600, 630)
  console.assert(arr2.length===1 && arr2[0]===600, 'enumerate 10:00..10:30 -> one slot at 600')
})()

// ===== MiniCalendar (week-start Monday) =====
function MiniCalendar({ base, today, onSelectDate }: { base: Date; today: Date; onSelectDate: (d: Date) => void }) {
  const [viewDate, setViewDate] = useState<Date>(() => new Date(base))
  useEffect(() => { setViewDate(new Date(base)) }, [base])

  const monthStart = useMemo(() => new Date(viewDate.getFullYear(), viewDate.getMonth(), 1), [viewDate])
  const monthEnd = useMemo(() => new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 0), [viewDate])
  const firstDow = (monthStart.getDay() + 6) % 7 // Monday=0
  const days: Date[] = []
  // fill leading days from previous month
  for (let i = 0; i < firstDow; i++) {
    days.push(new Date(monthStart.getFullYear(), monthStart.getMonth(), monthStart.getDate() - (firstDow - i)))
  }
  // current month days
  for (let d = 1; d <= monthEnd.getDate(); d++) days.push(new Date(viewDate.getFullYear(), viewDate.getMonth(), d))
  // trailing to 42 cells
  while (days.length % 7 !== 0 || days.length < 42) {
    const last = days[days.length - 1]
    days.push(new Date(last.getFullYear(), last.getMonth(), last.getDate() + 1))
  }

  const dow = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']

  return (
    <div className="w-[280px] select-none">
      <div className="flex items-center justify-between mb-2">
        <button className="px-2 py-1 rounded-lg hover:bg-gray-100" onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1))}>◀</button>
        <div className="text-sm font-semibold">{viewDate.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })}</div>
        <button className="px-2 py-1 rounded-lg hover:bg-gray-100" onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1))}>▶</button>
      </div>
      <div className="grid grid-cols-7 gap-1 text-xs mb-1 text-gray-600">
        {dow.map(d => <div key={d} className="text-center py-1">{d}</div>)}
      </div>
      <div className="grid grid-cols-7 gap-1">
        {days.map((d, i) => {
          const isOtherMonth = d.getMonth() !== viewDate.getMonth()
          const isToday = sameDate(d, today)
          return (
            <button
              key={i}
              className={`h-8 rounded-lg border text-xs ${isOtherMonth? 'bg-white border-gray-200 text-gray-400' : 'bg-gray-50 border-gray-200 text-gray-800 hover:bg-gray-100'} ${isToday? '!bg-blue-100 !border-blue-200 !text-blue-900' : ''}`}
              onClick={() => onSelectDate(d)}
            >
              {d.getDate()}
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default function OutlookLikeCalendarMock() {
  const [today] = useState(() => new Date())
  const todayStart = useMemo(() => startOfDay(today), [today])
  const [cursor, setCursor] = useState<Date>(() => startOfWeek(new Date()))
  const [view, setView] = useState<'week' | 'day'>('week')
  const browserTZ = Intl.DateTimeFormat().resolvedOptions().timeZone
  const [tz, setTz] = useState<string>(() => (RUS_TZS as readonly string[]).includes(browserTZ) ? browserTZ : 'Europe/Moscow')
  const [role, setRole] = useState<'DEV' | 'HR1' | 'HR2'>('DEV')
  const [eventTypes, setEventTypes] = useState<EventType[]>(DEFAULT_EVENT_TYPES)

  // Bookings state (includes some approved samples)
  const [bookings, setBookings] = useState<Booking[]>([
    { id: 'b1', dayIdx: 1, startMin: 11 * 60, duration: 120, createdBy: 'HR1', eventTypeName: 'Deep‑Dive', status: 'APPROVED', company: 'Hooli',      hrName: 'Dana', hrEmail: 'dana@hooli.example' },
    { id: 'b2', dayIdx: 3, startMin: 15 * 60, duration: 60,  createdBy: 'HR2', eventTypeName: 'Technical', status: 'APPROVED', company: 'Pied Piper', hrName: 'Gil',  hrEmail: 'gil@piper.example' },
  ])

  // Free slots explicit state (30m units) — start empty; developer adds
  const [freeSlots, setFreeSlots] = useState<FreeSlot[]>([])

  // Selection & highlight
  const [selected, setSelected] = useState<{ col: number; row: number } | null>(null)
  const [selectedDayIdx, setSelectedDayIdx] = useState<number>(() => (new Date().getDay() + 6) % 7)

  // Modal state
  type ModalState = { mode: 'create' | 'view'; open: boolean }
  const [modal, setModal] = useState<ModalState>({ mode: 'create', open: false })
  const [modalDayIdx, setModalDayIdx] = useState<number | null>(null)
  const [modalDay, setModalDay] = useState<Date | null>(null)
  const [modalStartMin, setModalStartMin] = useState<number | null>(null)
  const [chosenTypeId, setChosenTypeId] = useState<string | null>(null)
  const [chosenDuration, setChosenDuration] = useState<number>(60)
  const [modalBookingId, setModalBookingId] = useState<string | null>(null)
  const [confirmDecline, setConfirmDecline] = useState<boolean>(false)
  const [pendingCloseSlot, setPendingCloseSlot] = useState<{ dayIdx: number; startMin: number; date: Date } | null>(null)

  // Developer: availability bulk-add modal
  const [availOpen, setAvailOpen] = useState(false)
  const [availStart, setAvailStart] = useState<Date>(() => addDays(startOfWeek(new Date()), 0))
  const [availEnd, setAvailEnd] = useState<Date>(() => addDays(startOfWeek(new Date()), 0))
  const [availStartMin, setAvailStartMin] = useState<number>(10*60)
  const [availEndMin, setAvailEndMin] = useState<number>(12*60)
  const [availErr, setAvailErr] = useState<string>('')

  // Custom type input (light gray default that clears on focus)
  const MAX_TYPE_NAME_LEN = 18
  const DEFAULT_TEXT = 'Custom type'
  const [newTypeName, setNewTypeName] = useState(DEFAULT_TEXT)
  const [nameError, setNameError] = useState<string>('')

  // Booking details in modal
  const [company, setCompany] = useState('')
  const [hrName, setHrName] = useState('')
  const [hrEmail, setHrEmail] = useState('')

  // Create-mode validation UI
  const [bookingError, setBookingError] = useState<string>('')

  const weekStart = useMemo(() => startOfWeek(cursor), [cursor])
  const days = useMemo(() => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i)), [weekStart])
  const times = useMemo(() => {
    const out: Date[] = []
    const base = new Date(weekStart)
    for (let m = DAY_START; m < DAY_END; m += ROW_MINUTES) out.push(addMinutes(base, m))
    return out
  }, [weekStart])

  // Keyboard navigation (with proper cleanup)
  const gridRef = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const el = gridRef.current
    if (!el) return
    const handler = (e: KeyboardEvent) => {
      if (!['ArrowUp','ArrowDown','ArrowLeft','ArrowRight','Enter'].includes(e.key)) return
      if (!selected) { setSelected({ col: 0, row: 0 }); setSelectedDayIdx(0); e.preventDefault(); return }
      const maxRow = ROWS - 1, maxCol = 6
      let { col, row } = selected
      if (e.key === 'ArrowUp') row = Math.max(0, row - 1)
      if (e.key === 'ArrowDown') row = Math.min(maxRow, row + 1)
      if (e.key === 'ArrowLeft') col = Math.max(0, col - 1)
      if (e.key === 'ArrowRight') col = Math.min(maxCol, col + 1)
      if (e.key === 'Enter') { openCreateModal(col, days[col], DAY_START + row * ROW_MINUTES) }
      setSelected({ col, row }); setSelectedDayIdx(col); e.preventDefault()
    }
    el.addEventListener('keydown', handler)
    return () => { el.removeEventListener('keydown', handler) }
  }, [selected, days])

  // Date helpers
  const selectedDate = useMemo(() => addDays(weekStart, selectedDayIdx), [weekStart, selectedDayIdx])
  const isPastDate = (d: Date) => startOfDay(d).getTime() < todayStart.getTime()

  // Keep availability pickers aligned with current selection on open
  useEffect(() => {
    if (availOpen) { setAvailStart(selectedDate); setAvailEnd(selectedDate) }
  }, [availOpen, selectedDate])

  // Toolbar actions
  const goToday = () => { setCursor(startOfWeek(today)); setSelectedDayIdx((today.getDay()+6)%7) }
  const goPrevWeek = () => setCursor(addDays(cursor, -7))
  const goNextWeek = () => setCursor(addDays(cursor, 7))

  const prevDay = () => {
    const prev = addDays(selectedDate, -1)
    if (isPastDate(prev)) return
    const prevWeekStart = startOfWeek(prev)
    if (prevWeekStart.getTime() !== weekStart.getTime()) {
      setCursor(prevWeekStart)
      setSelectedDayIdx((prev.getDay()+6)%7)
    } else {
      setSelectedDayIdx((d)=>d-1)
    }
  }
  const nextDay = () => {
    const nxt = addDays(selectedDate, 1)
    const nxtWeekStart = startOfWeek(nxt)
    if (nxtWeekStart.getTime() !== weekStart.getTime()) {
      setCursor(nxtWeekStart)
      setSelectedDayIdx((nxt.getDay()+6)%7)
    } else {
      setSelectedDayIdx((d)=>d+1)
    }
  }

  // Open modals
  const openCreateModal = (dayIdx: number, day: Date, startMin: number) => {
    if (isPastDate(day)) return // do not open booking modal for past days
    setModal({ mode: 'create', open: true })
    setModalDayIdx(dayIdx); setModalDay(day); setModalStartMin(startMin)
    setChosenTypeId(null); setModalBookingId(null)
    setCompany(''); setHrName(''); setHrEmail('')
    setConfirmDecline(false); setBookingError(''); setChosenDuration(60)
  }
  const openViewModal = (booking: Booking) => {
    setModal({ mode: 'view', open: true })
    setModalBookingId(booking.id)
    setModalDayIdx(booking.dayIdx)
    setModalDay(addDays(weekStart, booking.dayIdx))
    setModalStartMin(booking.startMin)
    const et = eventTypes.find(e => e.name === booking.eventTypeName)
    setChosenTypeId(et?.id ?? null)
    setCompany(booking.company); setHrName(booking.hrName); setHrEmail(booking.hrEmail)
    setConfirmDecline(false)
  }

  // Custom event type management
  const addCustomType = () => {
    const raw = newTypeName === DEFAULT_TEXT ? '' : newTypeName.trim()
    if (!raw) { setNameError('Name required'); return }
    if (raw.length > MAX_TYPE_NAME_LEN) { setNameError(`Max ${MAX_TYPE_NAME_LEN} characters`); return }
    const lower = raw.toLowerCase()
    if (eventTypes.some(et => et.name.toLowerCase() === lower)) { setNameError('This event type already exists'); return }
    const id = `${raw.toLowerCase().replace(/[^a-z0-9]+/g,'-')}`
    const et = { id, name: raw }
    setEventTypes(prev => [...prev, et])
    setChosenTypeId(et.id)
    setNameError('')
  }

  // Availability helpers (30m units)
  const unitsNeeded = (durationMin: number) => Math.max(1, Math.ceil(durationMin / SLOT_UNIT))
  const hasContiguousUnits = (dayIdx: number, startMin: number, units: number) => {
    for (let k = 0; k < units; k++) {
      const uStart = startMin + k * SLOT_UNIT
      const exists = freeSlots.some(s => s.dayIdx === dayIdx && s.startMin === uStart)
      if (!exists) return false
    }
    return true
  }

  const canAddFreeSlot = (dayIdx: number, startMin: number) => {
    if (freeSlots.some(s => s.dayIdx === dayIdx && s.startMin === startMin)) return false
    if (bookings.some(b => overlapsSameDay(dayIdx, startMin, SLOT_UNIT, b))) return false
    return true
  }
  const addFreeSlot = (dayIdx: number, startMin: number) => {
    if (!canAddFreeSlot(dayIdx, startMin)) return false
    setFreeSlots(prev => {
      const next = [...prev, { dayIdx, startMin }]
      next.sort((a,b)=> a.dayIdx===b.dayIdx ? a.startMin-b.startMin : a.dayIdx-b.dayIdx)
      return next
    })
    return true
  }

  // Bulk add availability (Developer)
  const bulkAddAvailability = () => {
    setAvailErr('')
    const sDay = startOfDay(availStart)
    const eDay = startOfDay(availEnd)
    const sTime = Math.max(DAY_START, Math.min(DAY_END, availStartMin))
    const eTime = Math.max(DAY_START, Math.min(DAY_END, availEndMin))
    if (eTime - sTime < SLOT_UNIT) { setAvailErr('Time window must be at least 30 minutes.'); return }

    const next = [...freeSlots]
    // iterate inclusive from sDay..eDay
    for (let d = new Date(sDay); d.getTime() <= eDay.getTime(); d = addDays(d, 1)) {
      if (isPastDate(d)) continue
      // map to current week column
      const dayIdx = Math.round((startOfDay(d).getTime() - startOfDay(weekStart).getTime()) / (24*60*60*1000))
      if (dayIdx < 0 || dayIdx > 6) continue // only current grid week
      for (const m of enumerateStarts(sTime, eTime)) {
        const exists = next.some(fs => fs.dayIdx===dayIdx && fs.startMin===m)
        const conflicts = bookings.some(b => overlapsSameDay(dayIdx, m, SLOT_UNIT, b))
        if (!exists && !conflicts) next.push({ dayIdx, startMin: m })
      }
    }
    next.sort((a,b)=> a.dayIdx===b.dayIdx ? a.startMin-b.startMin : a.dayIdx-b.dayIdx)
    setFreeSlots(next)
    setAvailOpen(false)
  }

  // Create booking: require contiguous 30m units; then remove precisely those units
  const confirmBooking = () => {
    if (modalDayIdx == null || modalStartMin == null || !modalDay) return
    if (role === 'DEV') return
    if (isPastDate(modalDay)) return // guard: cannot book past
    const et = eventTypes.find(e => e.id === chosenTypeId)
    if (!et) { setNameError('Pick an event type'); return }
    if (!company.trim() || !hrName.trim() || !hrEmail.trim()) return
    const duration = chosenDuration
    const units = unitsNeeded(duration)
    if (!hasContiguousUnits(modalDayIdx, modalStartMin, units)) {
      setBookingError(`Not enough contiguous free time for ${duration} minutes.`)
      return
    }
    const id = cryptoRandomId()
    const newBooking: Booking = {
      id,
      dayIdx: modalDayIdx,
      startMin: modalStartMin,
      duration,
      createdBy: role === 'HR1' ? 'HR1' : 'HR2',
      eventTypeName: et.name,
      status: 'NOT_APPROVED',
      company: company.trim(),
      hrName: hrName.trim(),
      hrEmail: hrEmail.trim(),
    }
    setBookings(prev => [...prev, newBooking])
    // Remove exactly the consumed 30m unit slots
    setFreeSlots(prev => prev.filter(s => !(s.dayIdx === modalDayIdx && s.startMin >= modalStartMin && s.startMin < modalStartMin + units * SLOT_UNIT)))
    setModal({ ...modal, open: false })
  }

  // View/approve/decline modal actions (blocked on past days)
  const approveBooking = () => {
    if (!modalBookingId || (modalDay && isPastDate(modalDay))) return
    setBookings(prev => prev.map(b => b.id === modalBookingId ? { ...b, status: 'APPROVED' } : b))
    setModal({ ...modal, open: false })
  }
  const unapproveBooking = () => {
    if (!modalBookingId || (modalDay && isPastDate(modalDay))) return
    setBookings(prev => prev.map(b => b.id === modalBookingId ? { ...b, status: 'NOT_APPROVED' } : b))
    setModal({ ...modal, open: false })
  }
  const reallyDecline = () => {
    if (!modalBookingId || (modalDay && isPastDate(modalDay))) return
    const b = bookings.find(x => x.id === modalBookingId)
    if (!b) return
    // Remove booking
    setBookings(prev => prev.filter(x => x.id !== b.id))
    // Restore 30m units covering the booking duration (e.g., 120m -> 4×30m units)
    const units = unitsNeeded(b.duration)
    setFreeSlots(prev => {
      const next = [...prev]
      for (let k = 0; k < units; k++) {
        const uStart = b.startMin + k * SLOT_UNIT
        const exists = next.some(s => s.dayIdx === b.dayIdx && s.startMin === uStart)
        if (!exists) next.push({ dayIdx: b.dayIdx, startMin: uStart })
      }
      next.sort((a, c) => a.dayIdx === c.dayIdx ? a.startMin - c.startMin : a.dayIdx - c.dayIdx)
      return next
    })
    setModal({ ...modal, open: false })
    setConfirmDecline(false)
  }

  // UI helpers
  const weekStartDate = useMemo(() => startOfWeek(cursor), [cursor])
  // Layout metrics: each 30m row is 48px tall. Give blocks a tiny vertical padding so neighbors never collide.
  const ROW_PX = 48
  const V_PAD = 2
  const topPx = (startMin: number) => ((startMin - DAY_START) / ROW_MINUTES) * ROW_PX + V_PAD
  const heightPx = (durationMin: number) => Math.max(28, (durationMin / ROW_MINUTES) * ROW_PX - 2 * V_PAD)

  // Compute free slots by subtracting current bookings (defensive)
  const dayFreeSlots = (dayIdx: number) => freeSlots
    .filter(s => s.dayIdx === dayIdx)
    .filter(s => !bookings.some(b => overlapsSameDay(dayIdx, s.startMin, SLOT_UNIT, b)))

  // Common button class helpers with pressed states
  const btn = (base: string, active: string) => `${base} transition active:translate-y-[1px] active:shadow-none ${active}`

  // Range popover for week view
  const [rangeOpen, setRangeOpen] = useState(false)

  return (
    <div className="min-h-[720px] w-full bg-gray-50 text-gray-900 p-4">
      <div className="mx-auto max-w-7xl">
        {/* Main */}
        <section className="space-y-3">
          {/* Toolbar */}
          <div className="rounded-2xl bg-white shadow-sm p-3 flex items-center justify-between relative">
            <div className="flex items-center gap-2">
              <button className={btn('px-3 py-2 rounded-xl bg-gray-100 shadow-sm hover:bg-gray-200','active:bg-gray-300')} onClick={goToday}>Today</button>

              {view === 'week' ? (
                <div className="flex items-center gap-2">
                  <div className="flex rounded-xl overflow-hidden border border-gray-200">
                    <button className={btn('px-3 py-2 hover:bg-gray-100','active:bg-gray-200')} onClick={goPrevWeek} aria-label="Previous week">◀</button>
                    <button className="px-4 py-2 text-sm bg-gray-50 border-l border-r border-gray-200 hover:bg-gray-100" onClick={()=>setRangeOpen(o=>!o)} aria-haspopup="dialog">
                      {fmtMD(weekStartDate)} – {fmtMD(addDays(weekStartDate, 6))}
                    </button>
                    <button className={btn('px-3 py-2 hover:bg-gray-100','active:bg-gray-200')} onClick={goNextWeek} aria-label="Next week">▶</button>
                  </div>

                  {rangeOpen && (
                    <div className="absolute z-50 mt-2 top-12 left-28 rounded-xl bg-white border border-gray-200 shadow-lg p-3">
                      <MiniCalendar base={weekStartDate} today={today} onSelectDate={(d)=>{ setCursor(startOfWeek(d)); setSelectedDayIdx((d.getDay()+6)%7); setRangeOpen(false) }} />
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex rounded-xl overflow-hidden border border-gray-200">
                  <button
                    className={btn('px-3 py-2 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed','active:bg-gray-200')}
                    onClick={prevDay}
                    disabled={startOfDay(addDays(selectedDate, -1)) < startOfDay(today)}
                    aria-label="Previous day"
                  >
                    ◀
                  </button>
                  <div className="px-4 py-2 text-sm bg-gray-50 border-l border-r border-gray-200">
                    {fmtDay(selectedDate)} {fmtMD(selectedDate)}
                  </div>
                  <button className={btn('px-3 py-2 hover:bg-gray-100','active:bg-gray-200')} onClick={nextDay} aria-label="Next day">▶</button>
                </div>
              )}

              <div className="flex rounded-xl overflow-hidden border border-gray-200 ml-2">
                <button className={btn(`px-3 py-2 ${view==='day'?'bg-blue-600 text-white':'hover:bg-gray-100'}`,'active:bg-blue-700')} onClick={()=>{ setView('day'); setRangeOpen(false) }}>Day</button>
                <button className={btn(`px-3 py-2 ${view==='week'?'bg-blue-600 text-white':'hover:bg-gray-100'}`,'active:bg-blue-700')} onClick={()=>{ setView('week'); setRangeOpen(false) }}>Week</button>
              </div>

              {role==='DEV' && (
                <button className={btn('ml-2 px-3 py-2 rounded-xl bg-blue-600 text-white shadow-sm hover:bg-blue-700','active:bg-blue-800')} onClick={()=>{ setAvailOpen(true); setAvailErr('') }}>
                  + Add availability
                </button>
              )}
            </div>
            <div className="flex items-center gap-3">
              <label className="text-sm text-gray-600">Role</label>
              <select className="px-3 py-2 rounded-xl border border-gray-200 bg-white" value={role} onChange={e=>setRole(e.target.value as any)}>
                <option value="DEV">Developer</option>
                <option value="HR1">HR Manager 1</option>
                <option value="HR2">HR Manager 2</option>
              </select>
              <label className="text-sm text-gray-600">Time zone</label>
              <select className="px-3 py-2 rounded-xl border border-gray-200 bg-white" value={tz} onChange={e=>setTz(e.target.value)}>
                {(RUS_TZS as readonly string[]).map(z => <option key={z} value={z}>{z}</option>)}
              </select>
            </div>
          </div>

          {/* Calendar Grid */}
          <div
            ref={gridRef}
            tabIndex={0}
            role="grid"
            aria-label="Calendar grid"
            className="rounded-2xl bg-white shadow-sm p-2 outline-none"
          >
            {/* Header */}
            {(() => {
              const dayList = (view==='week')
                ? days.map((d, i) => ({ date: d, idx: i }))
                : [{ date: selectedDate, idx: selectedDayIdx }]
              return (
                <div className="grid" style={{ gridTemplateColumns: `80px repeat(${dayList.length}, minmax(0, 1fr))` }}>
                  <div className="h-10" />
                  {dayList.map(({date, idx}) => (
                    <div
                      key={idx}
                      className={`h-10 px-3 flex items-center border-l first:border-l-0 border-gray-200 ${selectedDayIdx===idx? 'bg-blue-50/60' : ''}`}
                      onClick={() => setSelectedDayIdx(idx)}
                    >
                      <div className="text-sm font-semibold">{fmtDay(date)}</div>
                      <div className="ml-2 text-xs text-gray-500">{fmtMD(date)}</div>
                    </div>
                  ))}
                </div>
              )
            })()}

            {/* Rows */}
            {(() => {
              const dayList = (view==='week')
                ? days.map((d, i) => ({ date: d, idx: i }))
                : [{ date: selectedDate, idx: selectedDayIdx }]
              return (
                <div className="grid" style={{ gridTemplateColumns: `80px repeat(${dayList.length}, minmax(0, 1fr))` }}>
                  {/* Time labels */}
                  <div className="relative">
                    {times.map((t, r) => (
                      <div key={r} className="h-12 border-t border-gray-100 pr-2 text-right text-xs text-gray-500 flex items-start justify-end">
                        <span className="-translate-y-2">{fmtTime(t)}</span>
                      </div>
                    ))}
                  </div>
                  {/* Day columns */}
                  {dayList.map(({date, idx}) => {
                    const dayBookings = bookings.filter(b => b.dayIdx === idx)
                    const dayFree = dayFreeSlots(idx)
                    const isSelected = selectedDayIdx === idx
                    const isPast = isPastDate(date)
                    return (
                      <div key={idx} className={`relative overflow-hidden border-l border-gray-100 ${isSelected? 'bg-blue-50/40' : ''}` } onMouseDown={()=>setSelectedDayIdx(idx)}>
                        {/* row grid + hover ghosts */}
                        {Array.from({length: ROWS}, (_, r) => {
                          const rowStart = DAY_START + r * ROW_MINUTES
                          const canGhost = role==='DEV' && !isPast && !dayFree.some(s=>s.startMin===rowStart) && !dayBookings.some(b=>overlaps(rowStart, SLOT_UNIT, b.startMin, b.duration))
                          return (
                            <div key={r} className={`relative h-12 border-t border-gray-50 ${selected?.col===idx && selected?.row===r ? 'bg-blue-50/70' : ''} group`}>
                              {canGhost && (
                                <button
                                  className="absolute inset-x-2 top-1 bottom-1 rounded-xl border border-dashed border-blue-300 bg-blue-50/60 opacity-0 group-hover:opacity-60 transition pointer-events-none group-hover:pointer-events-auto text-[10px] text-blue-800"
                                  onClick={() => addFreeSlot(idx, rowStart)}
                                  title="Add 30 min slot"
                                >
                                  + Add 30m
                                </button>
                              )}
                            </div>
                          )
                        })}

                        {/* Bookings (approved + not approved) */}
                        {dayBookings.map(b => {
                          const canOpen = role === 'DEV' || (role === 'HR1' && b.createdBy === 'HR1') || (role === 'HR2' && b.createdBy === 'HR2')
                          const occupied = !canOpen && (role === 'HR1' || role === 'HR2')
                          const baseCls = b.status==='APPROVED' ? 'bg-emerald-100 text-emerald-900 border-emerald-200' : 'bg-amber-100 text-amber-900 border-amber-200'
                          const occCls = occupied ? 'bg-gray-100 text-gray-500 border-gray-200' : baseCls
                          return (
                            <button
                              key={b.id}
                              disabled={occupied}
                              className={`absolute left-2 right-2 rounded-xl text-xs font-medium shadow-sm px-2 py-1 border ${occupied ? 'text-center flex items-center justify-center' : 'text-left'} transition active:translate-y-[1px] active:shadow-none ${occCls} ${isPast ? 'opacity-60' : ''}`}
                              style={{ top: topPx(b.startMin), height: heightPx(b.duration) }}
                              title={occupied ? 'Occupied' : `${b.eventTypeName} • ${b.duration}m • ${b.status === 'APPROVED' ? 'Approved' : 'Not approved'}`}
                              onClick={() => { if (canOpen) openViewModal(b) }}
                            >
                              <span className={`block ${b.duration >= 60 ? 'whitespace-normal break-words' : 'truncate'}`}>{occupied ? 'Occupied' : `${b.eventTypeName} • ${b.duration}m — ${b.status === 'APPROVED' ? 'Approved' : 'Not approved'}`}</span>
                              {!occupied && (
                                <span className={`block text-[10px] text-gray-700/90 ${b.duration >= 60 ? 'whitespace-normal break-words' : 'truncate'}`}>{b.company} • {b.hrName} • {b.hrEmail}</span>
                              )}
                            </button>
                          )
                        })}

                        {/* Free slots (buttons) */}
                        {dayFree.map((s, i) => (
                          <button
                            key={i}
                            disabled={isPast}
                            aria-disabled={isPast}
                            className={btn(`absolute left-2 right-2 rounded-xl ${isPast? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-blue-100 hover:bg-blue-200 text-blue-900'} text-xs font-medium shadow-sm px-2 py-1 truncate`, `${isPast? '' : 'active:bg-blue-300'}`)}
                            style={{ top: topPx(s.startMin), height: heightPx(SLOT_UNIT) }}
                            onClick={() => !isPast && role!=='DEV' && openCreateModal(idx, date, s.startMin)}
                          >
                            Free slot
                            {!isPast && role==='DEV' && (
                              <span
                                role="button"
                                aria-label="Close free slot"
                                className="absolute top-1 right-1 w-5 h-5 rounded-full bg-white border border-gray-300 text-gray-600 hover:bg-gray-50 flex items-center justify-center leading-none"
                                onClick={(e)=>{ e.stopPropagation(); setPendingCloseSlot({ dayIdx: idx, startMin: s.startMin, date }) }}
                              >
                                ×
                              </span>
                            )}
                          </button>
                        ))}
                      </div>
                    )
                  })}
                </div>
              )
            })()}
            <div className="p-2 text-xs text-gray-500">Tip: Past days are view-only. In Developer role, hover an empty row to reveal a semi-transparent "+ Add 30m"; click to create a slot. Use the toolbar button to bulk-add availability for a date/time range (current week).</div>
          </div>

          {/* Booking / View Modal */}
          {modal.open && (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/30" onClick={()=>setModal({ ...modal, open: false })} />
              <div className="relative w-[640px] max-w-[92vw] rounded-2xl bg-white shadow-xl p-5">
                {/* Header */}
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <div className="text-sm text-gray-500">{modal.mode==='create' ? 'Select event type & enter details' : 'Booking details'}</div>
                    {modalDay && modalStartMin!=null && (
                      <div className="text-base font-semibold">{fmtDay(modalDay)} {fmtMD(modalDay)} • {fmtTime(addMinutes(new Date(modalDay), modalStartMin))}</div>
                    )}
                  </div>
                  <button
                    className={btn('w-8 h-8 inline-flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-700 text-xl leading-none','active:bg-gray-200')}
                    aria-label="Close"
                    onClick={()=>setModal({ ...modal, open: false })}
                  >
                    ×
                  </button>
                </div>

                {/* Body */}
                {modal.mode === 'create' ? (
                  <>
                    {/* Event type chooser */}
                    <div className="max-h-48 overflow-auto pr-1 space-y-2 mb-3">
                      {eventTypes.map(et => (
                        <label key={et.id} className={`flex items-center justify-between rounded-xl border p-3 cursor-pointer transition ${chosenTypeId===et.id? 'border-blue-500 ring-2 ring-blue-100' : 'border-gray-200 hover:border-gray-300'}`}>
                          <div>
                            <div className="text-sm font-semibold truncate" title={et.name}>{et.name}</div>
                          </div>
                          <input type="radio" name="et" className="accent-blue-600" checked={chosenTypeId===et.id} onChange={()=>setChosenTypeId(et.id)} />
                        </label>
                      ))}
                    </div>

                    {/* Add custom type */}
                    <div className="p-3 rounded-xl border border-dashed border-gray-300 space-y-2 mb-3">
                      <div className="text-xs font-medium text-gray-600">Add custom type (≤ {MAX_TYPE_NAME_LEN} chars)</div>
                      <div className="flex gap-2 items-center">
                        <input
                          maxLength={MAX_TYPE_NAME_LEN}
                          className={`flex-1 px-2 py-1 rounded-lg border ${nameError? 'border-red-400' : 'border-gray-300'} ${newTypeName==='Custom type'? 'text-gray-400' : 'text-gray-900'}`}
                          value={newTypeName}
                          onFocus={()=>{ if (newTypeName==='Custom type') setNewTypeName('') }}
                          onBlur={()=>{ if (!newTypeName) setNewTypeName('Custom type') }}
                          onChange={e=>{ setNewTypeName(e.target.value); setNameError('') }}
                        />
                        <button className={btn('px-3 py-1 rounded-lg bg-blue-600 text-white shadow-sm hover:bg-blue-700','active:bg-blue-800')} onClick={addCustomType}>Add</button>
                      </div>
                      {nameError && <div className="text-xs text-red-600">{nameError}</div>}
                    </div>

                    {/* Booking details */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                      <div className="md:col-span-3 text-sm font-medium text-gray-700">Booking details</div>
                      <input className="px-3 py-2 rounded-xl border border-gray-300" placeholder="Company" value={company} onChange={e=>setCompany(e.target.value)} />
                      <input className="px-3 py-2 rounded-xl border border-gray-300" placeholder="HR name" value={hrName} onChange={e=>setHrName(e.target.value)} />
                      <input className="px-3 py-2 rounded-xl border border-gray-300" placeholder="HR email" type="email" value={hrEmail} onChange={e=>setHrEmail(e.target.value)} />
                    </div>

                    {/* Duration selector */}
                    <div className="mt-3">
                      <div className="text-xs font-medium text-gray-600 mb-1">Duration</div>
                      <div className="flex flex-wrap gap-2">
                        {[30,60,90,120].map(d => (
                          <button key={d} className={btn(`px-3 py-1 rounded-lg border ${chosenDuration===d? 'bg-blue-600 text-white border-blue-600' : 'bg-white border-gray-300 hover:bg-gray-50'}`,'active:bg-blue-700')} onClick={()=>setChosenDuration(d)}>{d} min</button>
                        ))}
                      </div>
                    </div>
                    {bookingError && <div className="mt-2 text-xs text-red-600">{bookingError}</div>}

                    <div className="mt-4 flex justify-end gap-2">
                      <button className={btn('px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200','active:bg-gray-300')} onClick={()=>setModal({ ...modal, open: false })}>Cancel</button>
                      <button className={btn('px-3 py-2 rounded-xl bg-blue-600 text-white disabled:opacity-50 shadow-sm hover:bg-blue-700','active:bg-blue-800')} disabled={role==='DEV' || !chosenTypeId || !company || !hrName || !hrEmail || (modalDay ? isPastDate(modalDay) : false)} onClick={confirmBooking}>Book</button>
                    </div>
                  </>
                ) : (
                  // VIEW MODE
                  <>
                    {(() => {
                      const b = bookings.find(x => x.id === modalBookingId)
                      if (!b) return <div className="text-sm text-red-600">Booking not found.</div>
                      const past = modalDay ? isPastDate(modalDay) : false
                      const isDev = role === 'DEV'
                      const canAct = isDev && !past
                      return (
                        <div className="space-y-3">
                          <div className="flex items-center justify-between">
                            <div className="text-sm">
                              <div><span className="font-medium">Event:</span> {b.eventTypeName} • {b.duration}m</div>
                            </div>
                            <div className={`px-2 py-1 rounded-lg text-xs font-medium ${b.status==='APPROVED' ? 'bg-emerald-100 text-emerald-900' : 'bg-amber-100 text-amber-900'}`}>{b.status==='APPROVED' ? 'Approved' : 'Not approved'}</div>
                          </div>
                          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                            <div className="px-3 py-2 rounded-xl bg-gray-50 border border-gray-200"><div className="text-[11px] text-gray-500">Company</div><div className="text-sm">{b.company}</div></div>
                            <div className="px-3 py-2 rounded-xl bg-gray-50 border border-gray-200"><div className="text-[11px] text-gray-500">HR name</div><div className="text-sm">{b.hrName}</div></div>
                            <div className="px-3 py-2 rounded-xl bg-gray-50 border border-gray-200"><div className="text-[11px] text-gray-500">HR email</div><div className="text-sm">{b.hrEmail}</div></div>
                          </div>

                          {/* Decline confirmation drawer */}
                          {confirmDecline && canAct && (
                            <div className="mt-2 p-3 rounded-xl border border-red-200 bg-red-50">
                              <div className="text-sm font-medium text-red-800">Decline this booking?</div>
                              <div className="text-xs text-red-700">The time will be restored as free 30‑minute slots.</div>
                              <div className="mt-2 flex justify-end gap-2">
                                <button className={btn('px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200','active:bg-gray-300')} onClick={()=>setConfirmDecline(false)}>Keep</button>
                                <button className={btn('px-3 py-2 rounded-xl bg-red-600 text-white shadow-sm hover:bg-red-700','active:bg-red-800')} onClick={reallyDecline}>Decline</button>
                              </div>
                            </div>
                          )}

                          <div className="mt-4 flex justify-end gap-2">
                            {!confirmDecline && isDev && (
                              <button className={btn('px-3 py-2 rounded-xl bg-red-600 text-white shadow-sm hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed','active:bg-red-800')} onClick={()=>!past && setConfirmDecline(true)} disabled={past}>Decline</button>
                            )}
                            {isDev && (b.status === 'APPROVED' ? (
                              <button className={btn('px-3 py-2 rounded-xl bg-amber-600 text-white shadow-sm hover:bg-amber-700 disabled:opacity-50 disabled:cursor-not-allowed','active:bg-amber-800')} onClick={unapproveBooking} disabled={past}>Mark unapproved</button>
                            ) : (
                              <button className={btn('px-3 py-2 rounded-xl bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed','active:bg-emerald-800')} onClick={approveBooking} disabled={past}>Approve</button>
                            ))}
                            <button className={btn('px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200','active:bg-gray-300')} onClick={()=>setModal({ ...modal, open: false })}>Close</button>
                          </div>
                        </div>
                      )
                    })()}
                  </>
                )}
              </div>
            </div>
          )}

          {/* Availability bulk-add modal */}
          {availOpen && role==='DEV' && (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/30" onClick={()=>setAvailOpen(false)} />
              <div className="relative w-[720px] max-w-[95vw] rounded-2xl bg-white shadow-xl p-5">
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <div className="text-base font-semibold">Add availability</div>
                    <div className="text-xs text-gray-600">Adds 30‑minute free slots across the selected date & time range in the <b>current week</b>.</div>
                  </div>
                  <button className={btn('w-8 h-8 inline-flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-700 text-xl leading-none','active:bg-gray-200')} onClick={()=>setAvailOpen(false)}>×</button>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <div className="text-xs font-medium text-gray-600 mb-1">Start date</div>
                    <MiniCalendar base={weekStartDate} today={today} onSelectDate={(d)=>setAvailStart(d)} />
                  </div>
                  <div>
                    <div className="text-xs font-medium text-gray-600 mb-1">End date</div>
                    <MiniCalendar base={weekStartDate} today={today} onSelectDate={(d)=>setAvailEnd(d)} />
                  </div>
                </div>
                <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <div className="text-xs font-medium text-gray-600 mb-1">Daily start time</div>
                    <input type="time" className="px-3 py-2 rounded-xl border border-gray-300" value={minToTime(availStartMin)} onChange={e=>setAvailStartMin(timeToMin(e.target.value))} />
                  </div>
                  <div>
                    <div className="text-xs font-medium text-gray-600 mb-1">Daily end time</div>
                    <input type="time" className="px-3 py-2 rounded-xl border border-gray-300" value={minToTime(availEndMin)} onChange={e=>setAvailEndMin(timeToMin(e.target.value))} />
                  </div>
                </div>
                {availErr && <div className="mt-2 text-xs text-red-600">{availErr}</div>}
                <div className="mt-4 flex justify-end gap-2">
                  <button className={btn('px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200','active:bg-gray-300')} onClick={()=>setAvailOpen(false)}>Cancel</button>
                  <button className={btn('px-3 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700','active:bg-blue-800')} onClick={bulkAddAvailability}>Add slots</button>
                </div>
              </div>
            </div>
          )}

          {/* Close free slot confirmation */}
          {pendingCloseSlot && (
            <div className="fixed inset-0 z-50 flex items-center justify-center">
              <div className="absolute inset-0 bg-black/30" onClick={()=>setPendingCloseSlot(null)} />
              <div className="relative w-[420px] max-w-[92vw] rounded-2xl bg-white shadow-xl p-5">
                <div className="text-sm font-medium text-red-800">Close this free slot?</div>
                <div className="text-xs text-red-700 mt-1">
                  {fmtDay(pendingCloseSlot.date)} {fmtMD(pendingCloseSlot.date)} • {fmtTime(addMinutes(new Date(pendingCloseSlot.date), pendingCloseSlot.startMin))}
                </div>
                <div className="mt-3 flex justify-end gap-2">
                  <button className={btn('px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200','active:bg-gray-300')} onClick={()=>setPendingCloseSlot(null)}>Keep</button>
                  <button className={btn('px-3 py-2 rounded-xl bg-red-600 text-white shadow-sm hover:bg-red-700','active:bg-red-800')} onClick={() => { if (!pendingCloseSlot) return; setFreeSlots(prev => prev.filter(s => !(s.dayIdx===pendingCloseSlot.dayIdx && s.startMin===pendingCloseSlot.startMin))); setPendingCloseSlot(null) }}>Close slot</button>
                </div>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

function cryptoRandomId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return (crypto as any).randomUUID()
  return 'id-' + Math.random().toString(36).slice(2)
}
