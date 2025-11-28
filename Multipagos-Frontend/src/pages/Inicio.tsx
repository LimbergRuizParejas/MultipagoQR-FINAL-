// src/pages/Inicio.tsx
import React from "react";

export default function Inicio() {
  return (
    <div className="grid gap-10 lg:grid-cols-[minmax(0,1.6fr)_minmax(0,1.1fr)] items-start">

      {/* ===========================
          COLUMNA IZQUIERDA – HERO
      ============================ */}
      <section className="space-y-6 animate-fade-in">
        <p className="text-[11px] font-semibold tracking-[0.25em] text-red-500 uppercase">
          Multipagos QR · Bolivia
        </p>

        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 leading-tight">
          Pagá tus servicios{" "}
          <span className="text-red-600">rápido, seguro</span> y sin crear una cuenta.
        </h1>

        <p className="text-slate-700 text-base sm:text-lg max-w-xl">
          Centralizá el pago de <strong>agua, luz, internet, colegios</strong> y otros
          servicios en un solo lugar. Solo necesitás tu{" "}
          <strong>CI, NIT o código de cliente</strong> y listo, el sistema busca tu deuda por vos.
        </p>

        {/* Ventajas */}
        <div className="flex flex-col gap-3 text-sm sm:text-base text-slate-800">
          {[
            "Sin registro ni contraseña.",
            "Pagos inmediatos mediante código QR.",
            "Comprobante descargable al instante.",
          ].map((text, idx) => (
            <div key={idx} className="flex items-center gap-2">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 text-xs font-bold">
                ✓
              </span>
              <span>{text}</span>
            </div>
          ))}
        </div>

        {/* Banner informativo */}
        <div className="mt-4 rounded-2xl border border-amber-100 bg-amber-50/80 px-4 py-3 text-sm text-amber-800 shadow-sm flex items-start gap-2 max-w-xl">
          <span className="mt-0.5 text-lg">💡</span>
          <p>
            Las <strong>empresas proveedoras</strong> tienen un panel especial donde pueden
            cargar sus deudas y revisar pagos en tiempo real.
          </p>
        </div>
      </section>

      {/* ===========================
          COLUMNA DERECHA – VIDEO (TikTok size)
      ============================ */}
      <section className="relative animate-fade-in-right flex justify-center">

        {/* Fondo decorativo */}
        <div className="pointer-events-none absolute inset-0 -z-10 rounded-3xl 
                        bg-gradient-to-br from-red-500/15 via-rose-400/12 to-orange-300/12 blur-2xl" />

        {/* Tarjeta de video grande */}
        <div className="bg-white/95 backdrop-blur rounded-3xl border border-slate-100 
                        shadow-[0_18px_45px_rgba(15,23,42,0.12)] p-6 sm:p-7 max-w-[450px] w-full">

          {/* Encabezado */}
          <div className="mb-4 flex items-center justify-between gap-2">
            <div>
              <h2 className="text-lg sm:text-xl font-semibold text-slate-900">
                Tutorial rápido
              </h2>
              <p className="text-xs sm:text-sm text-slate-500 mt-1">
                Mirá cómo usar la plataforma en segundos.
              </p>
            </div>

            <div className="hidden sm:flex flex-col items-end text-[11px] text-slate-500">
              <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-1 text-emerald-700">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                Demo visual
              </span>
              <span>Explicación rápida</span>
            </div>
          </div>

          {/* 🎥 VIDEO ESTILO TIKTOK */}
          <div className="rounded-xl overflow-hidden shadow-md">
            <video
              src="/videos/video.mp4"
              autoPlay
              loop
              muted
              playsInline
              preload="auto"
              className="w-full h-[600px] object-cover"     // 👈 ALTO TIKTOK
            />
          </div>

        </div>
      </section>
    </div>
  );
}
