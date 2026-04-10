<p align="center">
	<img src="logo.png" alt="PolyDroid Logo" />  
</p>
<sub>credits to @enderplaysvr on discord for logo</sub>

# PolyDroid

PolyDroid is an Android application that lets you to run the Windows version of Polytoria on mobile based on Winlator-CMOD and FEXCore.

## Installation

1. Download and install the APK from [GitHub Releases](https://github.com/cetotos/PolyDroid/releases/latest)
2. Launch the app, give permissions and wait for the installation process to finish (it will take a while, the game data and rootfs is ~4 GB)

## FAQ

### Is this against the Polytoria rules?

Since PolyDroid doesn't give you any cheats that give you advantages, it isn't bannable.

### Will you sell my data/Mine bitcoin from my phone; Is this a virus?

PolyDroid (and any Winlator that is older than 8.0) requires full file access for execution to work properly.
It copies game data to /sdcard which is fully writable by any app, however this requires full file access.
This is (probably) the same reason why Google Play protection flags this as dangerous.


Modern Winlator fixes this (8.0 and above) however it has some performance loss since it uses proot.


But, honestly, DON'T install any apps you dont trust from the web! If I were you I would also be skeptical.. 

### Why won't it run/Why does it run very poorly?

Since the Polytoria Client uses DirectX 11, DXVK is used. While modern DXVK is pretty good (2.x.x and above), it requires Vulkan 1.3 and above which not many devices have support for.

If you see the DXVK Sarek message on the bottom when launching a game, or the top left says DXVK-Sarek is being used, you will experience poor performance and graphical glitches.

PolyDroid 2 will attempt to fix this by using Vulkan instead, however it is not out yet.

### I can't login, it's stuck at cloudflare/I get ERR_NAME_NOT_RESOLVED

This is usually a network issue or cloudflare issue which can't be fixed by me.

ERR_NAME_NOT_RESOLVED is an Android WebView issue which I also can't fix sadly, however PolyDroid 2 should fix this as it just uses Chrome instead of WebView


## Credits and Third-party apps
- Winlator-CMOD ([github.com/Stredohori/Winlator-CMOD](https://github.com/Stredohori/Winlator-CMOD))
- FEXCore ([github.com/FEX-Emu/FEX](https://github.com/FEX-Emu/FEX))
- Ubuntu RootFs ([Focal Fossa](https://releases.ubuntu.com/focal))
- Wine ([winehq.org](https://www.winehq.org/))
- Box86/Box64 by [ptitseb](https://github.com/ptitSeb)
- Mesa (Turnip) ([mesa3d.org](https://www.mesa3d.org))
- DXVK ([github.com/doitsujin/dxvk](https://github.com/doitsujin/dxvk))
- CNC DDraw ([github.com/FunkyFr3sh/cnc-ddraw](https://github.com/FunkyFr3sh/cnc-ddraw))
- Polytoria-Controllers ([github.com/cetotos/Polytoria-Controllers](https://github.com/cetotos/Polytoria-Controllers))
- DXVK Sarek ([github.com/pythonlover02/DXVK-Sarek](https://github.com/pythonlover02/DXVK-Sarek))

Many thanks to [ptitSeb](https://github.com/ptitSeb) (Box86/Box64), [Danylo](https://blogs.igalia.com/dpiliaiev/tags/mesa/) (Turnip), [alexvorxx](https://github.com/alexvorxx) (Mods/Tips) and others.
Thank you to all the people who believe in this project.
And to the Polytoria community :)
