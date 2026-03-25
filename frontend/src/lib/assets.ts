const DDRAGON_VERSION = "16.6.1";

export function getChampionPortraitUrl(championKey: string): string {
  return `https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/img/champion/${championKey}.png`;
}
