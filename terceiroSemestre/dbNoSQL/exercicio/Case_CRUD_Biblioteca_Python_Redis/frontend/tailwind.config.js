/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
      colors: {
        ink: "#1f2933",
        moss: "#3f6f5f",
        paper: "#f7f4ed",
        cloud: "#eef2f3",
      },
      boxShadow: {
        soft: "0 18px 45px rgba(31, 41, 51, 0.12)",
      },
    },
  },
  plugins: [],
};
