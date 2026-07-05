# imhof

Imhof is a Java topographic map generator. It reads OpenStreetMap `.osm.gz`
files and SRTM `.hgt` elevation files, then renders PNG maps with shaded
relief, contour lines, vector features, and a title border.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

The project uses the standard Maven layout:

- `src/main/java`: application source
- `src/main/resources`: packaged runtime resources
- `src/test/java`: unit tests

## Build

```bash
mvn clean package
```

## Test

```bash
mvn test
```

## Run

```bash
mvn exec:java -Dexec.args="<osm_file> <dem_file> <lat_min> <lon_min> <lat_max> <lon_max> <dpi> <output_png> <title> [projection]"
```

Example:

```bash
mvn exec:java -Dexec.args="osm/lausanne.osm.gz dem/N46E006.hgt 46.50 6.55 46.57 6.70 300 output.png Lausanne CH1903"
```

Arguments:

- `osm_file`: path to an `.osm.gz` file
- `dem_file`: path to one `.hgt` elevation tile
- `lat_min`, `lon_min`, `lat_max`, `lon_max`: bounding box in degrees
- `dpi`: output resolution
- `output_png`: destination PNG path
- `title`: map title rendered in the border
- `projection`: optional, `CH1903` by default; also supports `Equirectangular`

The packaged jar is executable too:

```bash
java -jar target/imhof-1.0-SNAPSHOT.jar osm/lausanne.osm.gz dem/N46E006.hgt 46.50 6.55 46.57 6.70 300 output.png Lausanne CH1903
```
