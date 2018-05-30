/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.couchsc;

import com.inubit.research.client.JSONHttpRequest;
import java.io.FileInputStream;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.domainModel.DomainModel;
import net.frapu.couchsc.utils.CouchDBHelper;
import org.json.JSONObject;

/**
 *
 * @author frank
 */
public class ContentCreator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        DomainModel model = (DomainModel) ProcessUtils.parseProcessModelSerialization(
                new FileInputStream("resources/buchladen.model"));

        InstanceConnector ic = new InstanceConnector(model, "http://localhost:5984/", CouchDBHelper.getBasicAuthString("admin","admin"));

        // Erzeuge 1000 Autoren
        long startTime = System.currentTimeMillis();

        final int SIZE = 10;

        for (int i = 0; i < SIZE; i++) {
            // Create new Author doc
            JSONObject author = new JSONObject();
            author.put("type", "Autor");
            author.put("Name", "Author_" + i);
            author.put("Vorname", "James");
            author.put("Geburtsdatum", "" + ((int) (Math.random() * 28) + 1)
                    + "/" + ((int) (Math.random() * 12) + 1) + "/"
                    + ((int) (Math.random() * 90) + 1900));
            author.put("Veröffentlichungen", "" + ((int) (Math.random() * 10) + 1));
            // Publish document to DB
            //System.out.println(doc);
            try {
                ic.putDocument(author);
            } catch (Exception ex) {
                // Ignore error, they are in parsing the response...
            }

            // Erzeuge 10 Bücher je Autor
            for (int i1 = 0; i1 < 10; i1++) {
                JSONObject book = new JSONObject();
                book.put("type", "Buch");
                book.put("ISBN", "ISBN_" + i + "_" + i1);
                book.put("Titel", "Title_" + i + "_" + i1);
                book.put("Autor", author.get("_id"));
                try {
                    ic.putDocument(book);
                } catch (Exception ex) {
                    // Ignore error, they are in parsing the response...
                }

            }

            if (i % 10 == 0) {
                System.out.print("+");
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\nDuration (ms): " + (endTime - startTime) + " (" + ((endTime - startTime) / SIZE) + "ms/author)");

    }
}
