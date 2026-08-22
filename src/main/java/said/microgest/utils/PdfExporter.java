package said.microgest.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import said.microgest.entities.Adherent;
import said.microgest.entities.Epargne;
import said.microgest.entities.Operation;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Point d'entrée unique pour la génération de documents PDF
 * (relevés de compte, rapports, etc.).
 */
public class PdfExporter {

    private static final DateTimeFormatter DATE_HEURE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static File genererRelevePdf(
            Adherent adherent,
            Epargne epargne,
            List<Operation> operations
    ) throws Exception {

        File fichier = File.createTempFile(
                "releve_" + adherent.getNumeroAdherent() + "_",
                ".pdf"
        );

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        PdfWriter.getInstance(document, new FileOutputStream(fichier));

        document.open();

        Font titreFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font sousTitreFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font enteteFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

        Paragraph titre = new Paragraph(
                "MicroGest — Relevé de compte épargne", titreFont
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(10);
        document.add(titre);

        Paragraph infos = new Paragraph(
                "Adhérent : " + adherent.getFullName()
                        + "  |  N° Adhérent : " + adherent.getNumeroAdherent()
                        + "\nSolde actuel : "
                        + String.format("%,.0f FCFA", epargne.getSolde())
                        + "\nDate d'édition : "
                        + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                sousTitreFont
        );
        infos.setSpacingAfter(20);
        document.add(infos);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 3f, 4f});

        Color headerColor = new Color(44, 62, 80);

        for (String header : new String[]{"Type", "Montant", "Date", "Observation"}) {

            PdfPCell cell = new PdfPCell(new Phrase(header, enteteFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(6);
            table.addCell(cell);
        }

        if (operations == null || operations.isEmpty()) {

            PdfPCell vide = new PdfPCell(new Phrase("Aucune opération enregistrée.", cellFont));
            vide.setColspan(4);
            vide.setPadding(10);
            vide.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(vide);

        } else {

            for (Operation operation : operations) {

                table.addCell(new Phrase(
                        operation.getType() != null ? operation.getType().name() : "",
                        cellFont
                ));

                table.addCell(new Phrase(
                        String.format("%,.0f FCFA", operation.getMontant()), cellFont
                ));

                table.addCell(new Phrase(
                        operation.getDateOperation() != null
                                ? operation.getDateOperation().format(DATE_HEURE_FORMAT)
                                : "",
                        cellFont
                ));

                table.addCell(new Phrase(
                        operation.getObservation() != null ? operation.getObservation() : "",
                        cellFont
                ));
            }
        }

        document.add(table);

        document.close();

        return fichier;
    }
}