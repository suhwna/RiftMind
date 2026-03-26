const DDRAGON_VERSION = "16.6.1";

const CHAMPION_ICON_ALIASES: Record<string, string> = {
  chogath: "Chogath",
  drmundo: "DrMundo",
  fiddlesticks: "Fiddlesticks",
  jarvaniv: "JarvanIV",
  jax: "Jax",
  kaisa: "Kaisa",
  khazix: "Khazix",
  kogmaw: "KogMaw",
  ksante: "KSante",
  leblanc: "Leblanc",
  leesin: "LeeSin",
  masteryi: "MasterYi",
  missfortune: "MissFortune",
  monkeyking: "MonkeyKing",
  reksai: "RekSai",
  tahmkench: "TahmKench",
  twistedfate: "TwistedFate",
  velkoz: "Velkoz",
  xinzhao: "XinZhao",
};

export function getChampionPortraitUrl(championKey?: string | null, fallbackName?: string | null): string {
  const resolvedKey = normalizeChampionAssetKey(championKey) ?? normalizeChampionAssetKey(fallbackName) ?? "Aatrox";
  return `https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/img/champion/${resolvedKey}.png`;
}

function normalizeChampionAssetKey(value?: string | null): string | null {
  if (!value) {
    return null;
  }

  const compact = value.replace(/[^a-zA-Z0-9]/g, "");
  if (!compact) {
    return null;
  }

  const alias = CHAMPION_ICON_ALIASES[compact.toLowerCase()];
  if (alias) {
    return alias;
  }

  return compact;
}
