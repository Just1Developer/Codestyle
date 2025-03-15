# Codestyle
IntelliJ Codestyle plugin for Programming, adds additional checks to IntelliJ like minimal visibility.

# Installation (currently)

## The plugin is not on the marketplace yet, so you have to install it manually.

In the `Releases` Section on GitHub, you can find the latest release. Download the `Codestyle-1.0.0.zip` file and install it in IntelliJ.

1. Open IntelliJ
2. Go to `File` -> `Settings` -> `Plugins`
3. Click on the gear icon and select `Install Plugin from Disk...`
4. Select the downloaded `Codestyle-1.0.0.zip` file
5. Click `OK` and restart the IDE!
6. Done!

## Using the plugin

There are several ways to check for minimal visibility.
The plugin will add warnings to the code if the visibility is not minimal, these warnings show up on the method or when running a local inspection.
There is also a new tab in the sidebar called `Minimal Visibility`. With this, you can run a scan and with custom usage settings for protected and package-private.

### This plugin is still very much in development, so please report any issues you find.

## Alternative: Run as standalone IDE

You can of course also run this as a standalone IDE. To do this, you need to clone the repository and simply run:
```shell
./gradlew runIde
```

This will (download and) open a new IntelliJ IDE with the plugin installed.