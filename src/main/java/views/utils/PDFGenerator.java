package views.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import dao.ClasseDAO;
import dao.FichePedagogiqueDAO;
import models.*;

import java.awt.Desktop;
import java.io.File;
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
        
        // Récupérer la classe associée au cours
        Classe classe = ClasseDAO.findByCoursId(cours.getId());
        if (classe == null) {
            return false;
        }

        try {
            // Créer le document PDF
            Document document = new Document(PageSize.A4, 50, 50, 50, 50); // Marges de 50pt
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Ajouter l'en-tête institutionnel
            ajouterEntete(document);
            document.add(Chunk.NEWLINE);

            // Ajouter le titre
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
            Paragraph title = new Paragraph("FICHE DE SUIVI PÉDAGOGIQUE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Informations générales avec espacements réduits
            ajouterInfosGenerales(document, cours, enseignant, classe);
            // document.add(Chunk.NEWLINE);

            // Pourcentage de réalisation
            Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
            DecimalFormat df = new DecimalFormat("#.##");
            Paragraph pourcentage = new Paragraph();
            pourcentage.add(new Chunk("Pourcentage de réalisation : ", boldFont));
            pourcentage.add(new Chunk(df.format(fiche.getPourcentageRealisation()) + " %", normalFont));
            document.add(pourcentage);
            document.add(Chunk.NEWLINE);

            // Tableau des séances
            Font sectionFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            Paragraph titreSceance = new Paragraph("SÉANCES", sectionFont);
            titreSceance.setSpacingAfter(8f); // ajuste la valeur selon ton besoin (en points)
            document.add(titreSceance);
            ajouterTableauSeances(document, infos.getSeances());
            document.add(Chunk.NEWLINE);

            // Commentaires
            document.add(new Paragraph("COMMENTAIRES", sectionFont));
            if (fiche.getCommentaire() != null && !fiche.getCommentaire().isEmpty()) {
                document.add(new Paragraph(fiche.getCommentaire(), normalFont));
            } else {
                document.add(new Paragraph("Aucun commentaire", normalFont));
            }
            document.add(Chunk.NEWLINE);

            // Signature et date sur la même ligne
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            
            // Signature du Chef de Département (à gauche)
            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setBorder(Rectangle.NO_BORDER);
            Paragraph signature = new Paragraph("Signature du Chef de Département:", boldFont);
            signatureCell.addElement(signature);
            footerTable.addCell(signatureCell);
            
            // Date de génération (à droite)
            PdfPCell dateCell = new PdfPCell();
            dateCell.setBorder(Rectangle.NO_BORDER);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateGen = new Paragraph("Document généré le " + 
                                             fiche.getDateGeneration().format(formatter), 
                                             new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC));
            dateGen.setAlignment(Element.ALIGN_RIGHT);
            dateCell.addElement(dateGen);
            footerTable.addCell(dateCell);
            
            document.add(footerTable);

            document.close();
            
            // Ouvrir le PDF avec le lecteur par défaut
            ouvrirPDF(outputPath);
            
            return true;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère un nom de fichier PDF basé sur l'intitulé du cours
     * @param intituleCours L'intitulé du cours
     * @return Le nom de fichier formaté
     */
    public static String genererNomFichier(String intituleCours) {
        if (intituleCours == null || intituleCours.trim().isEmpty()) {
            return "cours.pdf";
        }
        
        // Convertir en minuscules et remplacer les espaces et caractères spéciaux par des tirets
        String nomFichier = intituleCours.toLowerCase()
            .replaceAll("[àáâãäå]", "a")
            .replaceAll("[èéêë]", "e")
            .replaceAll("[ìíîï]", "i")
            .replaceAll("[òóôõö]", "o")
            .replaceAll("[ùúûü]", "u")
            .replaceAll("[ýÿ]", "y")
            .replaceAll("[ç]", "c")
            .replaceAll("[^a-z0-9]", "-")  // Remplacer tout ce qui n'est pas alphanumerique par un tiret
            .replaceAll("-+", "-")        // Remplacer les tirets multiples par un seul
            .replaceAll("^-|-$", "");     // Supprimer les tirets en début et fin
        
        // S'assurer qu'on a un nom valide
        if (nomFichier.isEmpty()) {
            nomFichier = "cours";
        }
        
        return nomFichier + ".pdf";
    }

    /**
     * Ajoute l'en-tête institutionnel au document
     */
    private static void ajouterEntete(Document document) throws DocumentException, IOException {
        // Création d'un tableau pour l'en-tête
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[] {1, 3, 1});
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        
        // Logo de l'université (à gauche)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        
        // Charger l'image du logo
        String logoPath = "D:\\Documents\\GestionCahierTexte\\src\\main\\resources\\images\\logo-universite.jpg";
        Image logo = Image.getInstance(logoPath);
        // Redimensionner l'image si nécessaire
        logo.scaleToFit(70, 70);
        logo.setAlignment(Element.ALIGN_CENTER);
        
        logoCell.addElement(logo);
        headerTable.addCell(logoCell);
        
        // Informations centrales
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);
        Font deviseFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        
        Paragraph devise = new Paragraph("RÉPUBLIQUE DU SÉNÉGAL", deviseFont);
        devise.setAlignment(Element.ALIGN_CENTER);
        infoCell.addElement(devise);
        
        Paragraph ministere = new Paragraph("MINISTÈRE DE L'ENSEIGNEMENT SUPÉRIEUR", headerFont);
        ministere.setAlignment(Element.ALIGN_CENTER);
        infoCell.addElement(ministere);
        
        Paragraph universite = new Paragraph("UNIVERSITÉ IBA DER THIAM", headerFont);
        universite.setAlignment(Element.ALIGN_CENTER);
        infoCell.addElement(universite);
        
        Paragraph departement = new Paragraph("DÉPARTEMENT INFORMATIQUE", headerFont);
        departement.setAlignment(Element.ALIGN_CENTER);
        infoCell.addElement(departement);
        
        headerTable.addCell(infoCell);
        
        // Drapeau (à droite)
        PdfPCell flagCell = new PdfPCell();
        flagCell.setBorder(Rectangle.NO_BORDER);
        
        // Charger l'image du drapeau
        String drapeauPath = "D:\\Documents\\GestionCahierTexte\\src\\main\\resources\\images\\drapeau-pays.jpg";
        Image drapeau = Image.getInstance(drapeauPath);
        // Redimensionner l'image si nécessaire
        drapeau.scaleToFit(70, 70);
        drapeau.setAlignment(Element.ALIGN_CENTER);
        
        flagCell.addElement(drapeau);
        headerTable.addCell(flagCell);
        
        document.add(headerTable);
    }

    /**
     * Ajoute les informations générales au document
     */
    private static void ajouterInfosGenerales(Document document, Cours cours, Enseignant enseignant, Classe classe) 
            throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        
        // Enseignant (sans espacement supplémentaire)
        Paragraph enseignantInfo = new Paragraph();
        enseignantInfo.add(new Chunk("Enseignant : ", boldFont));
        enseignantInfo.add(new Chunk(enseignant.getPrenom() + " " + enseignant.getNom(), normalFont));
        document.add(enseignantInfo);
        
        // Classe (sans espacement supplémentaire)
        Paragraph classeInfo = new Paragraph();
        classeInfo.add(new Chunk("Classe : ", boldFont));
        classeInfo.add(new Chunk(classe.getNom(), normalFont));
        document.add(classeInfo);

        // Cours
        Paragraph coursInfo = new Paragraph();
        coursInfo.add(new Chunk("Cours : ", boldFont));
        coursInfo.add(new Chunk(cours.getIntitule(), normalFont));
        document.add(coursInfo);
    }

    /**
     * Ajoute le tableau des séances au document
     */
    private static void ajouterTableauSeances(Document document, java.util.List<Seance> seances) 
            throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] {2, 6, 1});
        table.setWidthPercentage(100);
        
        // En-têtes du tableau
        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        table.addCell(createHeaderCell("Date", headerFont));
        table.addCell(createHeaderCell("Contenu", headerFont));
        table.addCell(createHeaderCell("Validé", headerFont));
        
        // Données du tableau
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Seance seance : seances) {
            table.addCell(createCell(seance.getDateSeance().format(formatter), normalFont));
            table.addCell(createCell(seance.getContenu(), normalFont));
            
            // Utiliser "OUI" ou "NON" pour la validation
            PdfPCell validatedCell = new PdfPCell();
            validatedCell.setPadding(8);
            validatedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            validatedCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            if (seance.isEstValidee()) {
                validatedCell.setPhrase(new Phrase("OUI", normalFont));
            } else {
                validatedCell.setPhrase(new Phrase("NON", normalFont));
            }
            
            table.addCell(validatedCell);
        }
        
        document.add(table);
    }

    /**
     * Crée une cellule de tableau avec des propriétés personnalisées
     */
    private static PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
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
    
    /**
     * Ouvre le PDF avec le lecteur PDF par défaut du système
     */
    private static void ouvrirPDF(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.out.println("Le bureau n'est pas supporté sur ce système.");
                }
            } else {
                System.out.println("Le fichier spécifié n'existe pas.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}