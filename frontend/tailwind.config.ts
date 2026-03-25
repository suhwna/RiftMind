import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#0b1020",
        mist: "#eef2f7",
        tide: "#d6b36a",
        wave: "#8f7440",
        sand: "#121826",
        brass: "#d6b36a",
        slatepanel: "#161d2d"
      },
      boxShadow: {
        card: "0 16px 36px rgba(0, 0, 0, 0.22)"
      },
      fontFamily: {
        sans: ["Pretendard Variable", "SUIT Variable", "system-ui", "sans-serif"]
      }
    }
  },
  plugins: []
} satisfies Config;
