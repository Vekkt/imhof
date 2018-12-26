package ch.epfl.imhof.osm;

import ch.epfl.imhof.PointGeo;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;

public final class OSMMapReader {
    private OSMMapReader() {}

    public static OSMMap readOSMFile(String fileName, boolean unGZip) throws IOException, SAXException {
        OSMMap.Builder mapBuilder = new OSMMap.Builder();

        try (InputStream i = unGZip ? new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileName)))
                : new BufferedInputStream(new FileInputStream(fileName))) {

            XMLReader r = XMLReaderFactory.createXMLReader();

            r.setContentHandler(new DefaultHandler() {
                OSMEntity.Builder builder;

                public void startElement(String uri, String lName, String qName, Attributes atts) {
                    String id = atts.getValue("id");

                    switch (qName) {
                        case "node": {
                            double lon = Math.toRadians(Double.parseDouble(atts.getValue("lon")));
                            double lat = Math.toRadians(Double.parseDouble(atts.getValue("lat")));
                            builder = new OSMNode.Builder(Long.parseLong(id), new PointGeo(lon, lat));
                        }
                        break;
                        case "way": {
                            builder = new OSMWay.Builder(Long.parseLong(id));
                        }
                        break;
                        case "relation": {
                            builder = new OSMRelation.Builder(Long.parseLong(id));
                        }
                        break;
                        case "nd": {
                            long ref = Long.parseLong(atts.getValue("ref"));
                            if (mapBuilder.nodeForId(ref) == null)
                                builder.setIncomplete();
                            else
                                ((OSMWay.Builder) builder).addNode(mapBuilder.nodeForId(ref));
                        }
                        break;
                        case "tag": {
                            String key = atts.getValue("k");
                            if (key != null) {
                                String value = atts.getValue("v");
                                builder.setAttribute(key, value);
                            }
                        }
                        break;
                        case "member": {
                            if (builder.isIncomplete())
                                break;

                            OSMRelation.Member.Type type;
                            String role = atts.getValue("role");
                            long ref = Long.parseLong(atts.getValue("ref"));

                            switch (atts.getValue("type")) {
                                case "way": {
                                    if (mapBuilder.wayForId(ref) == null)
                                        builder.setIncomplete();
                                    else {
                                        type = OSMRelation.Member.Type.WAY;
                                        ((OSMRelation.Builder) builder)
                                                .addMember(type, role, mapBuilder.wayForId(ref));
                                    }
                                }
                                break;
                                case "node": {
                                    if (mapBuilder.nodeForId(ref) == null)
                                        builder.setIncomplete();
                                    else {
                                        type = OSMRelation.Member.Type.NODE;
                                        ((OSMRelation.Builder) builder)
                                                .addMember(type, role, mapBuilder.nodeForId(ref));
                                    }
                                }
                                break;
                                case "relation": {
                                    if (mapBuilder.relationForId(ref) == null)
                                        builder.setIncomplete();
                                    else {
                                        type = OSMRelation.Member.Type.RELATION;
                                        ((OSMRelation.Builder) builder)
                                                .addMember(type, role, mapBuilder.relationForId(ref));
                                    }
                                }
                            }

                        }
                        break;
                        default: {

                        }
                    }
                }

                public void endElement(String uri, String lName, String qName) {
                    if (qName.equals("way") || qName.equals("node") || qName.equals("relation")) {
                        if (builder != null && !builder.isIncomplete()) {
                            switch (qName) {
                                case "node": {
                                    mapBuilder.addNode(((OSMNode.Builder) builder).build());
                                }
                                break;
                                case "way": {
                                    mapBuilder.addWay(((OSMWay.Builder) builder).build());
                                }
                                break;
                                case "relation": {
                                    mapBuilder.addRelation(((OSMRelation.Builder) builder).build());
                                }
                                break;
                                default: {

                                }
                            }
                        }
                    }
                }
            });
            r.parse(new InputSource(i));

            System.out.println("OSM map done !");
            return mapBuilder.build();
        }
    }
}
