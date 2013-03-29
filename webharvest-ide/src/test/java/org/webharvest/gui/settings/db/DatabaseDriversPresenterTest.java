package org.webharvest.gui.settings.db;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.same;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.webharvest.runtime.database.DriverManager;

public class DatabaseDriversPresenterTest extends UnitilsTestNG {

    private static final String LOCATION = "/tmp/driver.jar";

    @RegularMock
    @InjectInto(property = "driverManager")
    private DriverManager mockDriverManager;

    @RegularMock
    private DatabaseDriversView mockView;

    private DatabaseDriverDTO dto;

    @TestedObject
    private DatabaseDriversPresenter presenter;

    @BeforeMethod
    public void setUp() {
        this.dto = new DatabaseDriverDTO(LOCATION);
        this.presenter = new DatabaseDriversPresenter(mockView);
    }

    @AfterMethod
    public void tearDown() {
        this.dto = null;
        this.presenter = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullViewNotAllowed() {
        new DatabaseDriversPresenter(null);
    }

    @Test
    public void testRegisterDriver() {
        mockView.addToList(same(dto));
        expectLastCall();
        mockDriverManager.addDriverResource(eq(new File(LOCATION).toURI()));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.registerDriver(dto);
    }

    @Test
    public void testUnregisterDriver() {
        mockView.removeFromList(same(dto));
        expectLastCall();
        mockDriverManager.removeDriverResource(eq(new File(LOCATION).toURI()));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.unregisterDriver(dto);
    }
}
