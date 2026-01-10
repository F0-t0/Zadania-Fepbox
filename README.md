# Fepbox-Questy

Plugin na **Minecraft (Paper) 1.21.4** dodający w pełni konfigurowalny system questów z nagrodami, wymaganymi przedmiotami, limitami wykonań oraz automatycznym sprawdzaniem postępu.

---

## Funkcje

- Tworzenie questów z poziomu gry
- Wielolinijkowe opisy wymagań (`message`)
- Nagrody jako dowolne itemy (z meta/NBT)
- Wymagane przedmioty z dowolną ilością (np. 128 diamentów)
- Kilka wymaganych przedmiotów na jeden quest
- Automatyczne sprawdzanie co X sekund
- Opcja wyłączenia auto-zakończenia (tylko ręczne)
- Limit wykonań questa na gracza (np. 1 raz, 3 razy)
- Trwały zapis danych (`quests.yml`, `playerdata.yml`)
- Pełna konfiguracja przez `config.yml`

---

## Wymagania (Minecraft)

- Serwer: Paper / Pufferfish / Purpur
- Wersja: Minecraft **1.21.4**
- Java: **21**

Plugin **nie jest przeznaczony** na Bukkit/Spigot bez Paper API.

---

## Instalacja

1. Wrzuć `Fepbox-Questy.jar` do folderu `plugins/`
2. Uruchom serwer
3. Skonfiguruj `config.yml`
4. Zrestartuj serwer (nie używaj `/reload` do aktualizacji kodu)

---

## Pliki

- `config.yml` – konfiguracja, wiadomości, permisje, auto-check
- `quests.yml` – nagrody i wymagania przedmiotowe (ustawiane komendami)
- `playerdata.yml` – aktywne questy oraz licznik wykonań gracza

---

## Komendy

### Dla graczy
- `/quest info <nazwa>` – wyświetla opis i wymagania questa

### Dla administracji
- `/quest create <nazwa>` – tworzy quest
- `/quest start <nazwa>` – rozpoczyna quest
- `/quest complete <nazwa>` – kończy quest i daje nagrody
- `/quest reward add [nazwa]` – dodaje nagrodę (item w ręce)
- `/quest reward set <numer> [nazwa]` – ustawia nagrodę
- `/quest reward remove <numer> [nazwa]` – usuwa nagrodę
- `/quest require add [ilosc] [nazwa]` – dodaje wymagany przedmiot
- `/quest require set <numer> [ilosc] [nazwa]` – ustawia wymagany przedmiot
- `/quest require remove <numer> [nazwa]` – usuwa wymagany przedmiot
- `/quest require list [nazwa]` – lista wymagań
- `/quest reload` – przeładowuje konfigurację pluginu

---

## Wymagane przedmioty (ilości 128+)

Wymagania liczone są po całym ekwipunku (stacki się sumują).

Przykład:
- wymaganie: DIAMOND x128
- gracz może mieć 2 stacki po 64
- przy ukończeniu questa plugin zabiera dokładnie 128 diamentów

Ustawianie:
- Trzymaj item w ręce
- `/quest require add 128 nazwaQuesta`

---

## Auto-zakończenie / tryb ręczny

Konfiguracja w `config.yml`:

completion:
  check-every-seconds: 10
  manual-only: false

- `manual-only: false` – quest kończy się automatycznie po spełnieniu wymagań
- `manual-only: true` – quest można zakończyć tylko komendą `/quest complete`

---

## Limit wykonań questa na gracza

Każdy quest ma własny limit:

quests:
  diamenty:
    enabled: true
    completion-limit: 3

- `completion-limit: 1` – quest tylko raz
- `completion-limit: 3` – maksymalnie 3 razy
- `completion-limit: 0` lub `-1` – brak limitu

Limit jest sprawdzany przy:
- `/quest start`
- auto-check
- `/quest complete`

---

## Permisje (config.yml)

permissions:
  admin: "fepbox.questy.admin"
  info: "fepbox.questy.info"
  player-complete: "fepbox.questy.complete"
  player-complete-enabled: false

- `info` – dostęp do `/quest info`
- `admin` – wszystkie komendy administracyjne
- `player-complete-enabled` – pozwala graczom kończyć questy komendą

---

## Informacje końcowe

- Plugin zapisuje dane trwałe – restart serwera nie resetuje questów
- Nie używaj `/reload` do aktualizacji JAR
- Wspiera wiele aktywnych questów jednocześnie
- Przeznaczony do produkcji na serwerach 1.21.4
