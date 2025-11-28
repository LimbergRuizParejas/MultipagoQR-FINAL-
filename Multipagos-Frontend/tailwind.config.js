// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  // Archivos donde Tailwind debe buscar clases
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],

  // ❌ Sin dark mode (solo tema claro)
  darkMode: false,

  theme: {
    extend: {
      fontFamily: {
        sans: ["system-ui", "Segoe UI", "Roboto", "sans-serif"],
      },

      // 🎨 Paleta de colores de la app
      colors: {
        // Azul reutilizable si lo necesitas
        primary: {
          DEFAULT: "#2563eb",
          light: "#3b82f6",
          dark: "#1d4ed8",
        },

        // Texto oscuro elegante
        secondary: {
          DEFAULT: "#0f172a",
        },

        // 🔴 Marca roja para navbar / botones principales
        brandRed: {
          DEFAULT: "#dc2626", // bg-brandRed
          light: "#ef4444",   // bg-brandRed-light
          dark: "#b91c1c",    // bg-brandRed-dark
        },

        // Fondo base de la app
        background: "#f3f4f6",
      },

      // Sombras suaves para cards / navbar
      boxShadow: {
        soft: "0 10px 25px rgba(15,23,42,0.08)",
        elevated: "0 15px 35px rgba(15,23,42,0.12)",
      },

      // Bordes un poco más redondeados
      borderRadius: {
        xl: "0.75rem",
        "2xl": "1rem",
      },

      // Contenedor centrado por defecto
      container: {
        center: true,
        padding: {
          DEFAULT: "1rem",
          md: "1.5rem",
          lg: "2rem",
        },
      },
    },
  },

  plugins: [],
};
