import { useMutation } from "@tanstack/react-query";
import { ArrowRight, Swords, Telescope, TrendingUp } from "lucide-react";
import type { FormEvent, ReactNode } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { SectionCard } from "../components/SectionCard";
import { syncSummoner } from "../lib/api";

const DEFAULT_MATCH_COUNT = 10;

function parseRiotId(input: string) {
  const [gameName, tagLine] = input.split("#");
  return {
    gameName: gameName?.trim() ?? "",
    tagLine: tagLine?.trim() ?? "",
  };
}

export function HomePage() {
  const navigate = useNavigate();
  const [riotId, setRiotId] = useState("");
  const [matchCount, setMatchCount] = useState(DEFAULT_MATCH_COUNT);

  const syncMutation = useMutation({
    mutationFn: syncSummoner,
    onSuccess: (response) => {
      navigate(`/summoners/${response.puuid}`);
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const { gameName, tagLine } = parseRiotId(riotId);

    syncMutation.mutate({
      gameName,
      tagLine,
      matchCount,
    });
  };

  return (
    <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
      <section className="rounded-[28px] border border-white/8 bg-[#111827] p-8 shadow-card">
        <p className="text-[11px] font-semibold uppercase tracking-[0.24em] text-tide">League of Legends match tracker</p>
        <h2 className="mt-6 max-w-3xl text-4xl font-semibold tracking-[-0.06em] text-white md:text-5xl">
          최근 경기 흐름을
          <br />
          전술 보드처럼 읽는 화면.
        </h2>
        <p className="mt-6 max-w-xl text-base leading-7 text-slate-400">
          Riot ID를 입력하면 최근 경기와 상세 기록을 한 곳에서 확인하고, 조건별로 원하는 경기만 바로 좁혀볼 수 있습니다.
        </p>

        <div className="mt-10 grid gap-4 sm:grid-cols-3">
          <InfoTile icon={<Telescope className="h-4 w-4" />} label="입력 기준" value="Riot ID" />
          <InfoTile icon={<Swords className="h-4 w-4" />} label="핵심 흐름" value="최근 경기" />
          <InfoTile icon={<TrendingUp className="h-4 w-4" />} label="탐색 방식" value="조건 필터" />
        </div>
      </section>

      <SectionCard
        title="전적 불러오기"
        description="Riot ID와 최근 경기 수를 선택한 뒤 바로 대시보드로 이동합니다."
      >
        <form className="space-y-5" onSubmit={handleSubmit}>
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-slate-300">Riot ID</span>
            <input
              value={riotId}
              onChange={(event) => setRiotId(event.target.value)}
              placeholder="예: Hide on bush#KR1"
              className="w-full rounded-[18px] border border-white/8 bg-[#0b1020] px-4 py-4 text-lg text-white outline-none transition placeholder:text-slate-500 focus:border-tide"
            />
          </label>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-slate-300">최근 경기 수</span>
            <select
              value={matchCount}
              onChange={(event) => setMatchCount(Number(event.target.value))}
              className="w-full rounded-[18px] border border-white/8 bg-[#0b1020] px-4 py-3 text-white outline-none transition focus:border-tide"
            >
              {[5, 10, 15, 20].map((value) => (
                <option key={value} value={value}>
                  최근 {value}경기
                </option>
              ))}
            </select>
          </label>

          <button
            type="submit"
            disabled={syncMutation.isPending}
            className="inline-flex w-full items-center justify-center gap-2 rounded-[18px] bg-tide px-5 py-4 text-base font-semibold text-ink transition hover:bg-brass disabled:cursor-not-allowed disabled:opacity-60"
          >
            {syncMutation.isPending ? "동기화 중..." : "전적 불러오기"}
            <ArrowRight className="h-4 w-4" />
          </button>

          {syncMutation.isError ? (
            <p className="rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
              {(syncMutation.error as Error).message}
            </p>
          ) : null}
        </form>
      </SectionCard>
    </div>
  );
}

type InfoTileProps = {
  icon: ReactNode;
  label: string;
  value: string;
};

function InfoTile({ icon, label, value }: InfoTileProps) {
  return (
    <div className="rounded-[20px] border border-white/8 bg-[#0f1522] p-4">
      <div className="flex items-center gap-2 text-tide">
        {icon}
        <p className="text-xs font-semibold uppercase tracking-[0.16em]">{label}</p>
      </div>
      <p className="mt-3 text-xl font-semibold tracking-[-0.03em] text-white">{value}</p>
    </div>
  );
}
