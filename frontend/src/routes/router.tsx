import { createBrowserRouter } from "react-router-dom";
import { AppLayout } from "../layouts/AppLayout";
import { HomePage } from "../pages/HomePage";
import { MatchDetailPage } from "../pages/MatchDetailPage";
import { SummonerDashboardPage } from "../pages/SummonerDashboardPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "summoners/:puuid", element: <SummonerDashboardPage /> },
      { path: "matches/:matchId", element: <MatchDetailPage /> },
    ],
  },
]);
