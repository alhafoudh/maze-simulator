package sk.freevision.mazesimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;

@XmlRootElement(name = "svg", namespace = "http://www.w3.org/2000/svg")
public class Maze {

    private List<Wall> walls;

    public static Maze load(String fileName) throws JAXBException, XMLStreamException {
        File file = Gdx.files.internal(fileName).file();
        JAXBContext jaxbContext = JAXBContext.newInstance(Maze.class);

        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(file));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Maze maze = (Maze) jaxbUnmarshaller.unmarshal(xmlStreamReader);
        return maze;
    }

    public void preparePhysics(World world) {
        for (Wall wall : walls)
            wall.preparePhysics(world);
    }

    public void render(Matrix4 projectionMatrix) {
        for (Wall wall : walls)
            wall.render(projectionMatrix);
    }

    public List<Wall> getWalls() {
        return walls;
    }

    @XmlElementWrapper(name = "g", namespace = "http://www.w3.org/2000/svg")
    @XmlElement(name = "line", namespace = "http://www.w3.org/2000/svg")
    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }

}
