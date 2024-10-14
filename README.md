# EcoNetAPI-java

This project provides a java wrapper over the unofficial Rheem EcoNet API. It can be leveraged to get information about
devices, to set things like operation mode or temperature setpoint, to listen to events device is generating, or to inspect
energy efficiency

**NOTE:** this is an unofficial API and thus could break without notice.

### Usage

Get an instance of the EcoNetAPI object like `EcoNetAPI.getInstance("EMAIL", "PASSWORD")`. This object can be stored and reused without fear of leaving open connections.

### Examples

1. Listing devices:

```
Optional<UserData> result = EcoNetAPI.getInstance(email, password).fetchUserData();
List<Equipment> equipment = result.get().getResults().getLocations().get(0).getEquipments()
```

2. Get current setpoint of a device:

```
EcoNetAPI.getInstance(email, password).fetchUserData().ifPresent(userData -> {
    Location location = userData.getResults().getLocations().get(0);
    Equipment equipment = location.getEquipments().get(0);
    int currentSetpoint = equipment.getSetpoint().getValue();
});
```

3. Set the mode of a device:

```
EcoNetAPI.getInstance(email, password).fetchUserData().ifPresent(userData -> {
    Location location = userData.getResults().getLocations().get(0);
    Equipment equipment = location.getEquipments().get(0);
    Modes currentMode = equipment.getModes();
    api.setMode(equipment.getDeviceName(), equipment.getSerialNumber(), Modes.VACATION_MODE);
});
```

4. Subscribe to device events:

```
EcoNetAPI.getInstance(email, password)
				.subscribeToEvents((message) -> logger.info(message.getTopic() + " " + message.getPayload()));
```

5. Fetching energy usage information:

```
EcoNetAPI.getInstance(email, password).fetchUserData().ifPresent(userData -> {
    Location location = userData.getResults().getLocations().get(0);
    Equipment equipment = location.getEquipments().get(0);
    EnergyData energyData = api.fetchEnergyUsage(equipment.getDeviceName(), equipment.getSerialNumber(), 8, 10, 2024)
        .get().getResults()
});
```

### Running tests

1. Create an `application.properties` file in `/src/test/resources/application.properties`
2. Add your email into the file like `econet_email=[EMAIL]`
3. Add your password into the file like `econet_password=[ACCOUNT_PASSWORD]`
4. Run

## Installing

This library is available as a GitHub package and can be installed by adding to your `pom.xml`

```
<dependency>
  <groupId>com.bigboxer23</groupId>
  <artifactId>EcoNetAPI-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

Since it is a GitHub package, GitHub requires maven authenticate as a valid user to fetch from their package repository.

Instructions:
1. Create `~/.m2/settings.xml` if it does not exist
2. Create a GitHub personal access token if you do not have one (`settings` -> `developer settings` -> `tokens classic`)
3. In below example need to update `username` to your own GitHub username.  Update `password` to include your GitHub
access token created in the previous step
4. Example file:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository />
  <interactiveMode />
  <usePluginRegistry />
  <offline />
  <pluginGroups />
  <servers>
    <server>
      <id>github</id>
      <username>[my username]</username>
      <password>[my GH Access Token]</password>
    </server>
  </servers>
  <mirrors />
  <proxies />
</settings>
```

