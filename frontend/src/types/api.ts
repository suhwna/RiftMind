export type SummonerSyncRequest = {
  gameName: string;
  tagLine: string;
  matchCount: number;
};

export type SummonerSyncResponse = {
  puuid: string;
  gameName: string;
  tagLine: string;
  requestedMatchCount: number;
  savedMatchCount: number;
  syncedAt: string;
};

export type SummonerProfileResponse = {
  puuid: string;
  gameName: string;
  tagLine: string;
  summonerId: string | null;
  accountId: string | null;
  profileIconId: number | null;
  summonerLevel: number | null;
  lastSyncedAt: string;
};

export type SummonerMatchSummaryResponse = {
  matchId: string;
  gameCreation: string;
  gameDuration: number;
  queueId: number;
  queueNameKo: string;
  gameMode: string;
  championName: string;
  championKey: string;
  championNameKo: string;
  teamPosition: string | null;
  teamPositionKo: string | null;
  kills: number;
  deaths: number;
  assists: number;
  totalDamageDealtToChampions: number;
  goldEarned: number;
  totalMinionsKilled: number;
  neutralMinionsKilled: number;
  visionScore: number;
  wardsPlaced: number;
  interpretationTags: string[];
  win: boolean;
};

export type SummonerMatchListResponse = {
  puuid: string;
  count: number;
  matches: SummonerMatchSummaryResponse[];
};

export type MatchParticipantResponse = {
  puuid: string;
  summonerName: string;
  championName: string;
  championKey: string;
  championNameKo: string;
  teamId: number | null;
  teamPosition: string | null;
  teamPositionKo: string | null;
  kills: number;
  deaths: number;
  assists: number;
  win: boolean;
  totalDamageDealtToChampions: number;
  goldEarned: number;
  totalMinionsKilled: number;
  neutralMinionsKilled: number;
  visionScore: number;
  wardsPlaced: number;
  wardsKilled: number;
  champLevel: number;
  item0: number;
  item1: number;
  item2: number;
  item3: number;
  item4: number;
  item5: number;
  item6: number;
  itemNames: string[];
  itemIconUrls: string[];
  itemDescriptions: string[];
  summoner1Id: number;
  summoner2Id: number;
  summonerSpellNames: string[];
  summonerSpellIconUrls: string[];
  summonerSpellDescriptions: string[];
  primaryRune: number | null;
  primaryRuneName: string | null;
  primaryRuneIconUrl: string | null;
  primaryRuneDescription: string | null;
  secondaryRune: number | null;
  secondaryRuneName: string | null;
  secondaryRuneIconUrl: string | null;
  secondaryRuneDescription: string | null;
  interpretationTags: string[];
  totalDamageTaken: number;
  doubleKills: number;
  tripleKills: number;
  quadraKills: number;
  pentaKills: number;
  largestKillingSpree: number;
  largestMultiKill: number;
  killingSprees: number;
  firstBloodKill: boolean;
  firstBloodAssist: boolean;
  firstTowerKill: boolean;
  firstTowerAssist: boolean;
  turretKills: number;
  inhibitorKills: number;
  damageDealtToObjectives: number;
  damageDealtToTurrets: number;
  objectivesStolen: number;
  objectivesStolenAssists: number;
  totalHeal: number;
  totalHealsOnTeammates: number;
  totalDamageShieldedOnTeammates: number;
};

export type MatchDetailResponse = {
  matchId: string;
  gameCreation: string;
  gameDuration: number;
  gameDurationText: string;
  queueId: number;
  queueNameKo: string;
  gameMode: string;
  gameVersion: string;
  focusStrengths: string[];
  focusWeaknesses: string[];
  participants: MatchParticipantResponse[];
};

export type SearchMatchResponse = {
  matchId: string;
  puuid: string;
  gameCreation: string;
  queueId: number;
  queueNameKo: string;
  gameMode: string;
  summonerName: string;
  championName: string;
  championKey: string;
  championNameKo: string;
  teamPosition: string | null;
  teamPositionKo: string | null;
  totalDamageDealtToChampions: number;
  goldEarned: number;
  totalMinionsKilled: number;
  neutralMinionsKilled: number;
  totalCs: number;
  visionScore: number;
  wardsPlaced: number;
  wardsKilled: number;
  champLevel: number;
  itemIds: number[];
  itemNames: string[];
  itemIconUrls: string[];
  summonerSpellIds: number[];
  summonerSpellNames: string[];
  summonerSpellIconUrls: string[];
  primaryRune: number | null;
  primaryRuneName: string | null;
  primaryRuneIconUrl: string | null;
  secondaryRune: number | null;
  secondaryRuneName: string | null;
  secondaryRuneIconUrl: string | null;
  interpretationTags: string[];
  kills: number;
  deaths: number;
  assists: number;
  kda: number;
  win: boolean;
  totalDamageTaken: number;
};

export type SearchMatchListResponse = {
  total: number;
  page: number;
  size: number;
  matches: SearchMatchResponse[];
};

export type SearchFilterChampionOptionResponse = {
  championName: string;
  championKey: string;
  championNameKo: string;
};

export type SearchFilterPositionOptionResponse = {
  teamPosition: string;
  teamPositionKo: string;
};

export type SearchFilterModeOptionResponse = {
  queueId: number;
  queueNameKo: string;
};

export type SearchFilterOptionsResponse = {
  champions: SearchFilterChampionOptionResponse[];
  positions: SearchFilterPositionOptionResponse[];
  modes: SearchFilterModeOptionResponse[];
};

export type SearchOverviewChampionResponse = {
  championName: string;
  championKey: string;
  championNameKo: string;
  matchCount: number;
  winCount: number;
  winRate: number;
  averageKda: number;
};

export type SearchOverviewPositionResponse = {
  teamPosition: string;
  teamPositionKo: string;
  matchCount: number;
  winCount: number;
  winRate: number;
  averageKda: number;
};

export type SearchOverviewResponse = {
  puuid: string;
  requestedMatchCount: number;
  analyzedMatchCount: number;
  totalIndexedMatches: number;
  winCount: number;
  lossCount: number;
  winRate: number;
  averageKda: number;
  averageDamage: number;
  averageGold: number;
  averageCs: number;
  averageVisionScore: number;
  topPlayedChampions: SearchOverviewChampionResponse[];
  mostPlayedChampion: SearchOverviewChampionResponse | null;
  bestChampion: SearchOverviewChampionResponse | null;
  mainChampionFrequentOpponentChampion: SearchOverviewChampionResponse | null;
  mainChampionToughestOpponentChampion: SearchOverviewChampionResponse | null;
  mainChampionFrequentItemNames: string[];
  toughestOpponentChampion: SearchOverviewChampionResponse | null;
  frequentOpponentChampion: SearchOverviewChampionResponse | null;
  bestPosition: SearchOverviewPositionResponse | null;
  weakPosition: SearchOverviewPositionResponse | null;
  frequentItemNames: string[];
  insights: string[];
};
