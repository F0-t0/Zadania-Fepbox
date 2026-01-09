Fepbox-Questy (Paper 1.21.4)

Komendy:
- /quest info <nazwa> (dla graczy)
- /quest create <nazwa> (admin)
- /quest start <nazwa> (admin)
- /quest reward <set|add|remove> <numer> [nazwa] (admin)
- /quest complete <nazwa> (admin lub gracz, zależnie od configu)
- /quest reload (admin)

Pliki:
- config.yml: wiadomości, uprawnienia, domyślne definicje wiadomości wymagań
- quests.yml: lista zadań + nagrody (ItemStack)
- playerdata.yml: aktywne zadania graczy i ukończone


Auto-check: config.yml -> completion (co ile sekund, i manual-only).
Wymagania: /quest require add [ilosc] [quest], set <nr> [ilosc] [quest], remove <nr> [quest], list [quest]
