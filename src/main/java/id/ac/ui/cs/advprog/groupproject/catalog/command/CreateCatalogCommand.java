package id.ac.ui.cs.advprog.groupproject.catalog.command;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateCatalogCommand {
    private final String name;
    private final String description;
    private final String imageUrl;
    private final Double price;
    private final Integer stock;
    private final String originLocation;
    private final LocalDate travelDate;

    public CreateCatalogCommand(
        String name,
        String description,
        String imageUrl,
        Double price,
        Integer stock,
        String originLocation,
        LocalDate travelDate
    ) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stock = stock;
        this.originLocation = originLocation;
        this.travelDate = travelDate;
    }
}
