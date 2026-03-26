import { NavLink, Outlet } from "react-router-dom";

const navItems = [{ to: "/", label: "홈" }];

export function AppLayout() {
  return (
    <div className="min-h-screen text-slate-100">
      <header className="sticky top-0 z-20 border-b border-slate-800 bg-[#0a0f19]/96 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
          <div className="space-y-1">
            <p className="text-[10px] font-semibold uppercase tracking-[0.34em] text-slate-500">League Report</p>
            <div className="flex items-baseline gap-2">
              <h1 className="text-[26px] font-extrabold uppercase tracking-[0.08em] text-white">RIFT</h1>
              <span className="text-[26px] font-extrabold uppercase tracking-[0.08em] text-tide">MIND</span>
            </div>
          </div>

          <nav className="flex gap-4">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  [
                    "border-b px-1 py-2 text-sm font-medium transition",
                    isActive ? "border-tide text-white" : "border-transparent text-slate-400 hover:border-slate-700 hover:text-slate-200",
                  ].join(" ")
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-6">
        <Outlet />
      </main>
    </div>
  );
}
