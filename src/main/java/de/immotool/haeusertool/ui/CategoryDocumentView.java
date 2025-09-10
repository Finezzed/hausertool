package de.immotool.haeusertool.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.*;
import de.immotool.haeusertool.docs.*;
import jakarta.annotation.security.PermitAll;

import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

@Route(value = "docs", layout = MainLayout.class) // technischer Haupt-Route
@RouteAlias("steuer")
@RouteAlias("allgemeine-rechnungen")
@RouteAlias("rechner-und-kalkulationen")
@RouteAlias("vorlagen")
@RouteAlias("banken")
@RouteAlias("allgemeine-infos")
@RouteAlias("vereine-und-firmen")
@RouteAlias("behoerden")
@RouteAlias("it-und-passwoerter")
@PageTitle("Dokumente")
@PermitAll
public class CategoryDocumentView extends VerticalLayout implements BeforeEnterObserver {

    private final FolderRepository folderRepo;
    private final FolderService folderService;
    private final DocumentRepository docRepo;
    private final DocumentService docService;

    private final TreeGrid<Folder> tree = new TreeGrid<>();
    private final Grid<Document> grid = new Grid<>(Document.class, false);

    private Folder selectedFolder;                 // null = Kategorie-Root
    private DocumentCategory selectedCategory;     // wird aus der URL abgeleitet

    public CategoryDocumentView(FolderRepository folderRepo, FolderService folderService,
                                DocumentRepository docRepo, DocumentService docService) {
        this.folderRepo = folderRepo; this.folderService = folderService;
        this.docRepo = docRepo; this.docService = docService;

        setSizeFull(); setPadding(true); setSpacing(true);

        var split = new SplitLayout(); split.setSizeFull(); split.setSplitterPosition(25);

        // --------- Linke Seite: Ordnerbaum + "Neuer Ordner" -----------
        tree.addHierarchyColumn(Folder::getName).setHeader("Ordner");
        tree.setSelectionMode(Grid.SelectionMode.SINGLE);
        tree.addSelectionListener(ev -> { selectedFolder = ev.getFirstSelectedItem().orElse(null); reloadDocs(); });

        var newFolderName = new TextField(); newFolderName.setPlaceholder("Neuer Ordner");
        var addFolder = new Button("Ordner anlegen", e -> {
            if (selectedCategory == null) { Notification.show("Kategorie fehlt"); return; }
            if (newFolderName.isEmpty()) { Notification.show("Name fehlt"); return; }
            var parentId = selectedFolder == null ? null : selectedFolder.getId();
            var created = folderService.create(newFolderName.getValue(), parentId, selectedCategory);
            newFolderName.clear(); reloadTree(); tree.select(created);
        });

        var left = new VerticalLayout(new HorizontalLayout(newFolderName, addFolder), tree);
        left.setSizeFull(); left.setPadding(false); tree.setSizeFull();
        split.addToPrimary(left);

        // --------- Rechte Seite: Upload + Liste -----------
        var title = new TextField("Titel"); title.setPlaceholder("Optional");

        var buffer = new FileBuffer();
        var upload = new Upload(buffer);
        upload.setDropAllowed(false);
        upload.setUploadButton(new Button("Datei auswählen")); // öffnet Dateidialog

        upload.addSucceededListener(ev -> {
            var file = buffer.getFileData().getFile().toPath();
            try (var in = Files.newInputStream(file)) {
                docService.store(in, ev.getFileName(), ev.getMIMEType(), ev.getContentLength(),
                        title.getValue(), selectedCategory, selectedFolder);
                title.clear(); reloadDocs();
                Notification.show("Hochgeladen: " + ev.getFileName());
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            } finally { try { Files.deleteIfExists(file); } catch (Exception ignore) {} }
        });

        var rightTop = new HorizontalLayout(title, upload);
        grid.addColumn(Document::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Document::getTitle).setHeader("Titel").setAutoWidth(true);
        grid.addColumn(Document::getOriginalFilename).setHeader("Datei").setAutoWidth(true);
        grid.addComponentColumn(d -> new Anchor("/files/"+d.getId()+"?inline=true","Öffnen"));
        grid.addComponentColumn(d -> new Button("Löschen", e -> { docService.delete(d.getId()); reloadDocs(); }));

        var right = new VerticalLayout(rightTop, grid);
        right.setSizeFull(); right.setPadding(false); grid.setSizeFull();
        split.addToSecondary(right);

        add(split);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Route-Pfad z. B. "steuer" -> Kategorie bestimmen
        var path = event.getLocation().getPath(); // ohne leading slash
        selectedCategory = mapPathToCategory(path);
        if (selectedCategory == null) selectedCategory = DocumentCategory.SONSTIGES; // Fallback

        reloadTree();
        reloadDocs();
    }

    private void reloadTree() {
        List<Folder> roots = folderService.roots(selectedCategory);
        tree.setItems(roots, f -> folderRepo.findByParentAndCategoryOrderByNameAsc(f, f.getCategory()));
    }

    private void reloadDocs() {
        if (selectedFolder == null) {
            grid.setItems(docRepo.findByFolderIsNullAndCategory(selectedCategory));
        } else {
            grid.setItems(docRepo.findByFolder(selectedFolder));
        }
    }

    private static DocumentCategory mapPathToCategory(String path) {
        var p = path.toLowerCase(Locale.ROOT);
        return switch (p) {
            case "steuer" -> DocumentCategory.STEUER;
            case "allgemeine-rechnungen" -> DocumentCategory.RECHNUNG;
            case "rechner-und-kalkulationen" -> DocumentCategory.VORLAGE; // oder eigene Enum falls gewünscht
            case "vorlagen" -> DocumentCategory.VORLAGE;
            case "banken" -> DocumentCategory.BANK;
            case "allgemeine-infos" -> DocumentCategory.SONSTIGES;
            case "vereine-und-firmen" -> DocumentCategory.SONSTIGES;
            case "behoerden" -> DocumentCategory.BEHOERDE;
            case "it-und-passwoerter" -> DocumentCategory.IT;
            case "docs" -> DocumentCategory.SONSTIGES; // Haupt-Route
            default -> null;
        };
    }
}

