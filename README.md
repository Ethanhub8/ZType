# ZType Game - VS Code Setup

## Project Structure
```
ztype-project/
├── .vscode/
│   └── settings.json    (VS Code Java settings)
├── lib/
│   ├── javalib.jar      (Fundies 2 graphics library)
│   └── tester.jar       (Fundies 2 tester library)
├── src/
│   └── ZType.java       (Game source code)
└── README.md
```

## Setup Instructions

### Prerequisites
1. **VS Code** with the **Extension Pack for Java** installed
   - Install from: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack
2. **JDK 11 or higher** installed on your system

### Running the Game

1. Open this folder in VS Code
2. Wait for Java extension to initialize (you'll see "Java" in the status bar)
3. Open `src/ZType.java`
4. Click "Run" above the `ExamplesZType` class, OR:
   - Right-click in the file → "Run Java"

### Running from Command Line
```bash
# Compile
javac -cp "lib/javalib.jar:lib/tester.jar" -d bin src/ZType.java

# Run (Mac/Linux)
java -cp "bin:lib/javalib.jar:lib/tester.jar" tester.Main ExamplesZType

# Run (Windows - use semicolons)
java -cp "bin;lib/javalib.jar;lib/tester.jar" tester.Main ExamplesZType
```

## Game Controls
- **Type letters** to destroy falling words
- **Space** to restart the game
- Words turn **red** when you start typing them
- Don't let words reach the bottom!

## Troubleshooting

### "Cannot resolve symbol 'tester'" or similar errors
- Make sure the `.vscode/settings.json` is present
- Reload VS Code window: `Cmd/Ctrl + Shift + P` → "Developer: Reload Window"

### Java extension not recognizing the project
- Delete any `.classpath` or `.project` files if they exist
- Ensure JDK is properly installed: run `java -version` in terminal
