package ch.epfl.imhof.osm;

import ch.epfl.imhof.PointGeo;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static ch.epfl.imhof.osm.OSMRelation.Member.Type.*;

/**
 * {@code public final class OSMMapReader}
 * <p>
 * Classe permettant la création d'une carte OSM à partir de données contenues
 * dans un fichier .osm (de type XML)
 *
 * @author Clément Nussbaumer (250261)
 * @author Leandro Kieliger (246263)
 */
public final class OSMMapReader {

    private final static String node = "node";
    private final static String way = "way";
    private final static String relation = "relation";
    private final static String nd = "nd";
    private final static String member = "member";
    private final static String tag = "tag";

    /**
     * {@code private OSMMapReader()}
     * <p>
     * Constructeur privé vide, empêchant l'instanciation d'OSMMapReader
     */
    private OSMMapReader() {
    }

    /**
     * {@code public static OSMMap readOSMFile(String fileName, boolean unGZip)
     * throws SAXException, IOException}
     * <p>
     * Méthode retournant un objet de type {@link OSMMap} en le créant à partir d'un
     * fichier au format .osm
     *
     * @param fileName - le chemin local vers le fichier
     * @param unGZip   - booléen indiquant s'il faut décompresser le fichier en entrée
     *                 (mettre {@code true} si le fichier est compressé au format gzip
     * @return Un objet {@link OSMMap}
     * @throws SAXException si le fichier XML contenant les données de la carte comporte
     *                      une erreur de format.
     * @throws IOException  en cas d'erreur de flux
     *                      d'entrée ou de sortie. Par exemple IOException est lancée si
     *                      le fichier n'existe pas.
     */
    public static OSMMap readOSMFile(String fileName, boolean unGZip) throws SAXException, IOException {

        OSMMap.Builder mapBuilder = new OSMMap.Builder();
        InputStream i = (unGZip) ?
                new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileName))) :
                new BufferedInputStream(new FileInputStream(fileName));

        XMLReader r = XMLReaderFactory.createXMLReader();
        /*
        Déclaration du gestionnaire de contenu du lecteur XML et redéfinition de celui-ci en classe anonyme
        pour traiter correctement un fichier xml au format OSM.
         */
        r.setContentHandler(new DefaultHandler() {
            OSMEntity.Builder currentElementBuilder;

            @Override
            public void startElement(String uri, String lName, String qName, Attributes atts) throws SAXException {

                switch (qName) {
                    case node:
                        //On instancie un builder de node
                        long id = Long.parseLong(atts.getValue("id"));
                        double lat = Double.parseDouble(atts.getValue("lat"));
                        double lon = Double.parseDouble(atts.getValue("lon"));
                        PointGeo p = new PointGeo(Math.toRadians(lon), Math.toRadians(lat));
                        currentElementBuilder = new OSMNode.Builder(id, p);
                        break;

                    case way:
                        // On instancie un builder de chemin
                        currentElementBuilder = new OSMWay.Builder(Long.parseLong(atts.getValue("id")));
                        break;

                    case relation:
                        // On instancie un builder de relation
                        currentElementBuilder = new OSMRelation.Builder(Long.parseLong(atts.getValue("id")));
                        break;

                    case nd:
                        // On ajoute un noeud au chemin
                        Long tmpNdId = Long.parseLong(atts.getValue("ref"));
                        if (mapBuilder.nodeForId(tmpNdId) == null) {
                            currentElementBuilder.setIncomplete();
                        } else {
                            ((OSMWay.Builder) currentElementBuilder).addNode(mapBuilder.nodeForId(tmpNdId));
                        }
                        break;

                    case member:
                        if (currentElementBuilder.isIncomplete())
                            break;
                        Long memberID = Long.parseLong(atts.getValue("ref"));
                        // On ajoute un membre à la relation
                        switch (atts.getValue("type")) {
                            case way:
                                if (mapBuilder.wayForId(memberID) == null) {
                                    currentElementBuilder.setIncomplete();
                                } else {
                                    ((OSMRelation.Builder) currentElementBuilder).addMember
                                            (WAY, atts.getValue("role"), mapBuilder.wayForId(memberID));
                                }
                                break;

                            case node:
                                if (mapBuilder.nodeForId(memberID) == null) {
                                    currentElementBuilder.setIncomplete();
                                } else {
                                    ((OSMRelation.Builder) currentElementBuilder).addMember
                                            (NODE, atts.getValue("role"), mapBuilder.nodeForId(memberID));
                                }
                                break;

                            case relation:
                                if (mapBuilder.relationForId(memberID) == null) {
                                    currentElementBuilder.setIncomplete();
                                } else {
                                    ((OSMRelation.Builder) currentElementBuilder).addMember
                                            (RELATION, atts.getValue("role"), mapBuilder.relationForId(memberID));
                                }
                                break;
                        }
                        break;

                    case tag:
                        // On ajoute un attribut à l'entité en construction.
                        String k = atts.getValue("k");
                        if (k != null) currentElementBuilder.setAttribute(k, atts.getValue("v"));
                        break;
                }
            }

            @Override
            public void endElement(String uri, String lName, String qName) {
                if (currentElementBuilder != null && !currentElementBuilder.isIncomplete()) {
                    switch (qName) {
                        case node:
                            // On construit et on ajoute un noeud au Builder de la map
                            mapBuilder.addNode(((OSMNode.Builder) currentElementBuilder).build());
                            break;

                        case way:
                            // On construit et on ajoute un chemin au builder de la map,
                            mapBuilder.addWay(((OSMWay.Builder) currentElementBuilder).build());
                            break;

                        case relation:
                            // On construit et on ajoute une relation au builder de la
                            mapBuilder.addRelation(((OSMRelation.Builder) currentElementBuilder).build());
                            break;
                    }
                }
            }

        });


        // Parcourt le fichier .osm puis ferme le flot entrant
        r.parse(new InputSource(i));
        i.close();
        return mapBuilder.build();
    }
}
