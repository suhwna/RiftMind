import { NavLink, Outlet } from "react-router-dom";

const navItems = [{ to: "/", label: "홈" }];

export function AppLayout() {
  return (
    <div className="min-h-screen text-slate-100">
      <header className="sticky top-0 z-20 border-b border-white/8 bg-[#0b111c]/92 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
          <div className="flex items-center gap-4">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-white/10 bg-[#131a28] text-sm font-semibold text-tide">
              RM
            </div>
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-500">Tactical match board</p>
              <h1 className="mt-1 text-[26px] font-semibold tracking-[-0.04em] text-white">RiftMind</h1>
            </div>
          </div>

          <nav className="flex gap-2 rounded-full border border-white/8 bg-[#111827] p-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  [
                    "rounded-full px-4 py-2 text-sm font-medium transition",
                    isActive ? "bg-tide text-ink" : "text-slate-300 hover:bg-white/5",
                  ].join(" ")
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}
