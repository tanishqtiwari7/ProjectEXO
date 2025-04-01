
# ProjectEXO - Secure Network Communications Server 🔒

A robust server implementation for encrypted client-server communications with RSA encryption and integrated AI capabilities.

## Features ✨

- 🔐 RSA 2048-bit encryption for secure data transmission
- 👥 Multiple client connection handling with unique UUID assignment
- 🔑 Secure key exchange protocol implementation
- 🔒 Username/password authentication system
- 📡 Chunked packet-based communication for large messages
- 🌐 Real-time client status broadcasting
- 🤖 Integrated AI functionality using Ollama
- 🖥️ JavaFX-based server management interface

## System Requirements 🖥️

- Java 21 or higher
- Maven 3.11+ (for building)
- Any operating system that supports Java
- Ollama running locally for AI functionality
- 4GB+ RAM recommended
- 100MB+ storage space

## AI Integration 🧠

ProjectEXO integrates with Ollama to provide AI capabilities:

- Uses gemma3:1b model by default
- Maintains conversation history for each connected client
- Handles AI prompts through the established secure connection
- Supports context-aware responses
- Allows switching between available models through the UI

### Setting Up Ollama

1. Install Ollama from [ollama.ai](https://ollama.ai)
2. Pull the default model: `ollama pull gemma3:1b`
3. Ensure Ollama is running when starting ProjectEXO

## Server Management Interface 🖱️

The server includes a JavaFX-based management interface that provides:

- Real-time monitoring of connected clients
- User registration management
- AI model selection and configuration
- Server status information
- Time elapsed since server start

## Building from Source 🛠️

### Using Maven Directly

```bash
# Clone the repository
git clone https://github.com/UdayKhare09/ProjectEXO.git
cd ProjectEXO

# Build the project
mvn clean package
```

### Using Build Scripts

**Windows:**
```
build.cmd
```

**Linux/macOS:**
```
chmod +x build.sh
./build.sh
```

## Running the Server 🚀

### Using Run Scripts

**Windows:**
```
run.cmd
```

**Linux/macOS:**
```
chmod +x run.sh
./run.sh
```

### Manually

```bash
java -jar target/ProjectEXO-1.0-SNAPSHOT.jar
```

## How It Works 🧩

- Server initializes and listens on port 2005
- Each client connection is assigned a unique UUID
- Secure RSA key exchange establishes encrypted communications
- Authentication verifies user credentials against stored username/password pairs
- Each client is handled in a separate thread for concurrent connections
- Packets are split into chunks for handling large messages
- All communications are encrypted using RSA encryption
- AI requests are processed through Ollama and responses returned securely

## Communication Protocol 📦

The server handles different types of packets:
- Type 0: Client status broadcasts
- Type 1: Regular messages
  - Subtype 0: General broadcast messages
  - Subtype 1: Private messages
- Type 9: AI functionality
  - Subtype 1: Text completion/chat

## Security Implementation 🔐

- 2048-bit RSA encryption for all communications
- Unique key pairs for each client connection
- Secure key exchange protocol
- Password-based authentication
- UUID-based client identification

## User Management 👤

- Users are stored in `~/.exo/server/known.txt`
- Default users are created on first run
- New users can be registered through the server interface
- Passwords are stored in plaintext (consider implementing hashing in future versions)

## Companion Project 🤝

This server works with the [ProjectEXO Client](https://github.com/UdayKhare09/ProjectEXO_Client) application.

## Contributing 💡

Contributions are welcome! Please feel free to submit a Pull Request.

## Contact Information 📞

- **Developer:** Uday Khare
- **Email:** udaykhare77@gmail.com
- **LinkedIn:** https://linkedin.com/in/uday-khare-a09208289
- **Portfolio:** https://portfolio.udaykhare.social
- **GitHub:** UdayKhare09

## License ⚖️
<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/93/GPLv3_Logo.svg/2560px-GPLv3_Logo.svg.png" alt="alt text" width="200" height="100">

This project is licensed under the [GNU General Public License v3 (GPLv3)](LICENSE).
