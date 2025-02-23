package de.koedev.distribution.view;

import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

@Route("")
@PageTitle("Distribution Test")
public class TestView extends VerticalLayout {
    @PostConstruct
    public void init() {
        setSizeFull();
        NativeLabel label = new NativeLabel("TEST");
        add(label);
    }
}
