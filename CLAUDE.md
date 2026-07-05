# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Imhof is a Swiss-style topographic map generator written in pure Java. It processes OpenStreetMap data and SRTM digital elevation models to produce high-quality PNG maps with shaded relief, contour lines, and styled vector features.

## Build & Run

This is a Maven project using the standard source layout. Requires JDK 21+ and Maven 3.9+.

**Build:**
```bash
mvn clean package
```

**Test:**
```bash
mvn test
```

**Run:**
```bash
mvn exec:java -Dexec.args="<osm_file> <dem_file> <lat_min> <lon_min> <lat_max> <lon_max> <dpi> <output_png> <title> [projection]"
```

or:

```bash
java -jar target/imhof-1.0-SNAPSHOT.jar \
    <osm_file> <dem_file> <lat_min> <lon_min> <lat_max> <lon_max> \
    <dpi> <output_png> <title> [projection]
```

**Arguments:**
- `osm_file`: Path to .osm.gz file (e.g., `osm/lausanne.osm.gz`)
- `dem_file`: Path to one .hgt elevation tile (e.g., `dem/N46E006.hgt`)
- `lat_min, lon_min, lat_max, lon_max`: Bounding box in degrees
- `dpi`: Output resolution (e.g., 300)
- `output_png`: Output file path
- `title`: Map title for border
- `projection`: Optional, "CH1903" (default) or "Equirectangular"

## Architecture

### Data Flow Pipeline
```
OSM File (.osm.gz) → OSMMapReader → OSMMap → OSMToGeoTransformer → Map
HGT Files → HGTDigitalElevationModel → ReliefShader → Shaded relief
                                     → Contours → Contour lines
Map + Relief + Contours → SwissPainter → Java2DCanvas → PNG
```

### Key Packages

- **`ch.epfl.imhof`** - Core model: `Map`, `PointGeo`, `Attributes`, `Vector3`
- **`ch.epfl.imhof.osm`** - OSM parsing: `OSMMapReader` (SAX parser), `OSMNode/Way/Relation`, `OSMToGeoTransformer`
- **`ch.epfl.imhof.dem`** - Elevation: `HGTDigitalElevationModel` (SRTM format), `ReliefShader`
- **`ch.epfl.imhof.geometry`** - Primitives: `Point`, `PolyLine`, `OpenPolyLine`, `ClosedPolyLine`, `Polygon`
- **`ch.epfl.imhof.projection`** - Coordinate transforms: `CH1903Projection`, `EquirectangularProjection`
- **`ch.epfl.imhof.contours`** - Altitude lines: `Contours`, `IsoCell` (marching squares)
- **`ch.epfl.imhof.painting`** - Rendering: `Painter` (composable), `SwissPainter`, `Java2DCanvas`, `Color`, `LineStyle`

### Key Patterns

- **Builder Pattern**: Used extensively for immutable objects (`Map.Builder`, `OSMMap.Builder`, `Attributes.Builder`, `PolyLine.Builder`)
- **Painter Composition**: `Painter` interface with fluent API - `painter1.above(painter2).when(predicate).layered()`
- **Immutability**: Records (`Point`, `Vector3`) and immutable collections throughout

### Input Data

- **OSM files**: `osm/` directory contains .osm.gz files (berne, interlaken, lausanne, montblanc, etc.)
- **DEM files**: `dem/` directory contains SRTM .hgt files (N45E006.hgt, N46E006.hgt, etc.)
- **Reference outputs**: `maps/` directory contains example PNG outputs

## Planned Features (from todo.md)

- City and place names
- Summit elevations
