

Android implementation for https://github.com/Johamatt/RR-API

# Installation

## Prequirements
- Mapbox account
- Google account
- Android studio 
- JRE 17+
- SDK 24+

## Installing Mapbox SDK
https://docs.mapbox.com/android/maps/guides/install/

### Obtain Default Public Token:
1. Visit [Mapbox Access Tokens](https://account.mapbox.com/access-tokens) and acquire the "Default public token."

2. Create an XML file named developer-config.xml in app/res/values/ directory.
    Paste the obtained token into the XML file as follows:

```developer-config.xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <string name="mapbox_access_token" translatable="false" tools:ignore="UnusedResources">pk.1234qwerty</string>
</resources>
```

### Generate Secret Access Token:
3. Create new secret access token from [Mapbox Access Tokens](https://account.mapbox.com/access-tokens) with the "Downloads:Read scope" selected 
4. Navigate to «USER_HOME»/.gradle/gradle.properties and add following to make it global gradle variable
```
MAPBOX_DOWNLOADS_TOKEN=sk.1234qwerty....
```

## Generate Google OAuth client ID
(This same ID is also required in backend)

1. Visit https://console.cloud.google.com/apis/credentials/ and navigate to Credentials -tab
2. Press "+ Create Credentials" -> Oauth Client ID
3. Select Web application and press "Create" using default values.
4. Copy Client ID and paste it in developer-config.xml as "web_client_id" 