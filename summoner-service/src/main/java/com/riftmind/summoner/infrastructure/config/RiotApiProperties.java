package com.riftmind.summoner.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Riot API 연동 설정 값을 바인딩하는 설정 클래스입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@ConfigurationProperties(prefix = "riftmind.riot")
public class RiotApiProperties {

    private String accountBaseUrl;
    private String summonerBaseUrl;
    private String matchBaseUrl;
    private String dataDragonBaseUrl = "https://ddragon.leagueoflegends.com";
    private String dataDragonRealm = "kr";
    private String dataDragonLocale = "ko_KR";
    private String apiKey;
    private int matchFetchMaxCount = 20;

    /**
     * Account API 기본 URL을 반환합니다.
     *
     * @return Account API 기본 URL
     */
    public String getAccountBaseUrl() {
        return accountBaseUrl;
    }

    /**
     * Account API 기본 URL을 설정합니다.
     *
     * @param accountBaseUrl Account API 기본 URL
     */
    public void setAccountBaseUrl(String accountBaseUrl) {
        this.accountBaseUrl = accountBaseUrl;
    }

    /**
     * Summoner API 기본 URL을 반환합니다.
     *
     * @return Summoner API 기본 URL
     */
    public String getSummonerBaseUrl() {
        return summonerBaseUrl;
    }

    /**
     * Summoner API 기본 URL을 설정합니다.
     *
     * @param summonerBaseUrl Summoner API 기본 URL
     */
    public void setSummonerBaseUrl(String summonerBaseUrl) {
        this.summonerBaseUrl = summonerBaseUrl;
    }

    /**
     * Match API 기본 URL을 반환합니다.
     *
     * @return Match API 기본 URL
     */
    public String getMatchBaseUrl() {
        return matchBaseUrl;
    }

    /**
     * Match API 기본 URL을 설정합니다.
     *
     * @param matchBaseUrl Match API 기본 URL
     */
    public void setMatchBaseUrl(String matchBaseUrl) {
        this.matchBaseUrl = matchBaseUrl;
    }

    /**
     * Data Dragon 기본 URL을 반환합니다.
     *
     * @return Data Dragon 기본 URL
     */
    public String getDataDragonBaseUrl() {
        return dataDragonBaseUrl;
    }

    /**
     * Data Dragon 기본 URL을 설정합니다.
     *
     * @param dataDragonBaseUrl Data Dragon 기본 URL
     */
    public void setDataDragonBaseUrl(String dataDragonBaseUrl) {
        this.dataDragonBaseUrl = dataDragonBaseUrl;
    }

    /**
     * Data Dragon realm 값을 반환합니다.
     *
     * @return Data Dragon realm 값
     */
    public String getDataDragonRealm() {
        return dataDragonRealm;
    }

    /**
     * Data Dragon realm 값을 설정합니다.
     *
     * @param dataDragonRealm Data Dragon realm 값
     */
    public void setDataDragonRealm(String dataDragonRealm) {
        this.dataDragonRealm = dataDragonRealm;
    }

    /**
     * Data Dragon locale 값을 반환합니다.
     *
     * @return Data Dragon locale 값
     */
    public String getDataDragonLocale() {
        return dataDragonLocale;
    }

    /**
     * Data Dragon locale 값을 설정합니다.
     *
     * @param dataDragonLocale Data Dragon locale 값
     */
    public void setDataDragonLocale(String dataDragonLocale) {
        this.dataDragonLocale = dataDragonLocale;
    }

    /**
     * Riot API 키를 반환합니다.
     *
     * @return Riot API 키
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Riot API 키를 설정합니다.
     *
     * @param apiKey Riot API 키
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 최대 매치 조회 수를 반환합니다.
     *
     * @return 최대 매치 조회 수
     */
    public int getMatchFetchMaxCount() {
        return matchFetchMaxCount;
    }

    /**
     * 최대 매치 조회 수를 설정합니다.
     *
     * @param matchFetchMaxCount 최대 매치 조회 수
     */
    public void setMatchFetchMaxCount(int matchFetchMaxCount) {
        this.matchFetchMaxCount = matchFetchMaxCount;
    }
}
