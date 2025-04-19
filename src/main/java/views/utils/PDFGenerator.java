package views.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import dao.ClasseDAO;
import dao.FichePedagogiqueDAO;
import models.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    /**
     * Génère un PDF à partir d'une fiche pédagogique
     * @param ficheId ID de la fiche pédagogique
     * @param outputPath Chemin de sortie du fichier PDF
     * @return true si la génération a réussi, false sinon
     */
    public static boolean genererPDF(int ficheId, String outputPath) {
        // Récupérer toutes les informations nécessaires
        FichePedagogiqueDAO.FicheInfosComplete infos = FichePedagogiqueDAO.getInfosComplete(ficheId);
        if (infos == null) {
            return false;
        }

        FichePedagogique fiche = infos.getFiche();
        Cours cours = infos.getCours();
        Enseignant enseignant = infos.getEnseignant();
        
        // Récupérer la classe associée au cours (nécessite d'implémenter une méthode dans ClasseDAO)
        Classe classe = ClasseDAO.findByCoursId(cours.getId());
        if (classe == null) {
            return false;
        }

        try {
            // Créer le document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Ajouter le titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("FICHE DE SUIVI PÉDAGOGIQUE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Informations générales
            ajouterInfosGenerales(document, cours, enseignant, classe);
            document.add(Chunk.NEWLINE);

            // Pourcentage de réalisation
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            DecimalFormat df = new DecimalFormat("#.##");
            Paragraph pourcentage = new Paragraph("Pourcentage de réalisation : " + 
                                                 df.format(fiche.getPourcentageRealisation()) + " %", sectionFont);
            document.add(pourcentage);
            document.add(Chunk.NEWLINE);

            // Tableau des séances
            document.add(new Paragraph("SÉANCES", sectionFont));
            ajouterTableauSeances(document, infos.getSeances());
            document.add(Chunk.NEWLINE);

            // Commentaires
            if (fiche.getCommentaire() != null && !fiche.getCommentaire().isEmpty()) {
                document.add(new Paragraph("COMMENTAIRES", sectionFont));
                document.add(new Paragraph(fiche.getCommentaire()));
                document.add(Chunk.NEWLINE);
            }

            // Date de génération
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateGen = new Paragraph("Document généré le " + 
                                             fiche.getDateGeneration().format(formatter), 
                                             new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            dateGen.setAlignment(Element.ALIGN_RIGHT);
            document.add(dateGen);

            document.close();
            return true;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ajoute les informations générales au document
     */
    private static void ajouterInfosGenerales(Document document, Cours cours, Enseignant enseignant, Classe classe) 
            throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        // Cours
        table.addCell(createCell("Cours :", boldFont));
        table.addCell(createCell(cours.getIntitule(), normalFont));
        
        // Enseignant
        table.addCell(createCell("Enseignant :", boldFont));
        table.addCell(createCell(enseignant.getPrenom() + " " + enseignant.getNom(), normalFont));
        
        // Classe
        table.addCell(createCell("Classe :", boldFont));
        table.addCell(createCell(classe.getNom(), normalFont));
        
        document.add(table);
    }

    /**
     * Ajoute le tableau des séances au document
     */
    private static void ajouterTableauSeances(Document document, java.util.List<Seance> seances) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] {2, 6, 1});
        table.setWidthPercentage(100);
        
        // En-têtes du tableau
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        table.addCell(createHeaderCell("Date", headerFont));
        table.addCell(createHeaderCell("Contenu", headerFont));
        table.addCell(createHeaderCell("Validé", headerFont));
        
        // Données du tableau
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Seance seance : seances) {
            table.addCell(createCell(seance.getDateSeance().format(formatter), normalFont));
            table.addCell(createCell(seance.getContenu(), normalFont));
            table.addCell(createCell(seance.isEstValidee() ? "✓" : "□", normalFont));
        }
        
        document.add(table);
    }

    /**
     * Crée une cellule de tableau avec des propriétés personnalisées
     */
    private static PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Crée une cellule d'en-tête de tableau avec des propriétés personnalisées
     */
    private static PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = createCell(text, font);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }
}