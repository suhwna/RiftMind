/**
 * RiftMind 헤더용 브랜드 마크입니다.
 */
export function RiftMindMark() {
  return (
    <div className="relative flex h-11 w-11 items-center justify-center border border-slate-700 bg-[#111827]">
      <span className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-tide/80 to-transparent" />
      <svg
        viewBox="0 0 44 44"
        aria-hidden="true"
        className="h-7 w-7"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path d="M10 33L18 11H23L15 33H10Z" className="fill-tide" />
        <path d="M21 33L29 11H34L26 33H21Z" className="fill-white/90" />
        <path d="M18.5 24H27.5" stroke="currentColor" strokeWidth="2" className="text-slate-400" />
        <circle cx="22" cy="24" r="2.5" className="fill-amber-300" />
      </svg>
    </div>
  );
}
