# CapyCoin 🍊

**[English](#english) · [Español](#español)**

---

## English

A capybara-themed personal finance app for Android: income, fixed expenses, savings and credit cards with installments, month by month.

### Features

- **Monthly summary** with the capybara in the bath: the water rises with the % of your income you've used.
- **Income**: one-time, recurring (with an optional end date) or in installments; refunds linked to expenses.
- **Expenses** grouped by day, in COP or USD (official exchange rate from Banco de la República), with paid and ignored marks.
- **Wallet-style credit cards**: installments with interest (French amortization), extra payments, early payoff, card fee with spending waiver and credit limit used.
- **Savings**, payment reminders, Capy tips and a monthly CSV report.
- **File backups**, privacy mode and fingerprint lock.
- **Express mode** for the cover screen of Galaxy Z Flip phones.
- **English and Spanish**: choose the language in Settings.

### Tech

Kotlin · Jetpack Compose (Material 3) · Room · Coroutines/Flow · minSdk 24 (with `java.time` desugaring).

### Build

1. Open the folder in Android Studio (or run `./gradlew assembleDebug`).
2. Signing uses `app/keystore/capycoin.jks`, which is **not** in the repository. Without it, debug builds use Android's standard debug key. To sign with your own key, create a keystore at that path and add to `local.properties`:

   ```properties
   capycoin.storePassword=...
   capycoin.keyAlias=...
   capycoin.keyPassword=...
   ```

### Tests

```bash
./gradlew testDebugUnitTest
```

### Credits

[Nunito](https://fonts.google.com/specimen/Nunito) font under the SIL Open Font License (see `app/src/main/assets/licenses/Nunito-OFL.txt`).

### Author

**Juan Garcia** — [garciagjs@javeriana.edu.co](mailto:garciagjs@javeriana.edu.co)

### License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Español

App Android de finanzas personales con temática de capibara: ingresos, gastos fijos, ahorros y tarjetas de crédito con cuotas, todo mes a mes.

### Funciones

- **Resumen del mes** con el capibara en la tina: el agua sube según el % de ingresos usado.
- **Ingresos** únicos, recurrentes (con fecha límite opcional) y en cuotas; reembolsos vinculados a gastos.
- **Gastos** agrupados por día, en COP o USD (TRM del Banco de la República), con marcas de pagado e ignorado.
- **Tarjetas de crédito** tipo billetera: cuotas con interés (sistema francés), abonos, liquidación anticipada, cuota de manejo con exoneración y cupo usado.
- **Ahorros**, recordatorios de pago, Capy-consejos y reporte CSV mensual.
- **Copias de seguridad** en archivo, modo privado y bloqueo con huella.
- **Modo Express** para la pantalla exterior de los Galaxy Z Flip.
- **Español e inglés**: el idioma se elige en Ajustes.

### Tecnología

Kotlin · Jetpack Compose (Material 3) · Room · Coroutines/Flow · minSdk 24 (con desugaring de `java.time`).

### Compilar

1. Abre la carpeta en Android Studio (o usa `./gradlew assembleDebug`).
2. La firma usa `app/keystore/capycoin.jks`, que **no** está en el repositorio. Sin ella, la versión debug se firma con la llave de debug estándar de Android. Para firmar con tu propia llave, crea el keystore en esa ruta y agrega a `local.properties`:

   ```properties
   capycoin.storePassword=...
   capycoin.keyAlias=...
   capycoin.keyPassword=...
   ```

### Tests

```bash
./gradlew testDebugUnitTest
```

### Créditos

Fuente [Nunito](https://fonts.google.com/specimen/Nunito) bajo licencia SIL Open Font License (ver `app/src/main/assets/licenses/Nunito-OFL.txt`).

### Autor

**Juan Garcia** — [garciagjs@javeriana.edu.co](mailto:garciagjs@javeriana.edu.co)

### Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
