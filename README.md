# Elastic Balls

A simple Java Swing project demonstrating elastic collision animation of bouncing balls.

## Features
- Multiple balls with basic collision response
- Adjustable window size and repaint loop
- Pure Java (AWT/Swing)

## Requirements
- JDK 8+ (tested on latest LTS)
- Windows/macOS/Linux

## Getting Started

### Build & Run
```bash
# from the project root
javac ElasticBalls.java
java ElasticBalls
```

### Run in an IDE
- Open the folder in your IDE (IntelliJ IDEA, Eclipse, VS Code + Java).
- Run the `ElasticBalls` class.

## Project Structure
```
Elastic Balls/
 ElasticBalls.java    # Main class with UI and animation loop
 README.md            # Project documentation
```

## Customization
You can tweak constants inside `ElasticBalls.java`, such as:
- Ball count and initial positions
- Velocity magnitudes
- Repaint delay / timer interval
- Canvas dimensions and colors

## Troubleshooting
- If `javac` is not recognized, ensure JDK is installed and `JAVA_HOME`/PATH are set.
- On some systems, high-DPI displays may need UI scaling adjustments in your IDE.

