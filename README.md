
# ProjectEXO - Secure Network Communications Server 🔒

A robust server implementation for encrypted client-server communications with RSA encryption and integrated AI capabilities.

## Features ✨

- 🔐 RSA encryption for secure data transmission
- 👥 Multiple client connection support
- 🔑 Key exchange protocol implementation
- 🔒 Secure authentication system
- 📡 Packet-based communication
- 🌐 Client status broadcasting
- 🤖 Integrated AI functionality using Ollama

## System Requirements 🖥️

- Java 21 or higher
- Maven 3.11+ (for building)
- Any operating system that supports Java
- Ollama running locally for AI functionality

## AI Integration 🧠

ProjectEXO integrates with Ollama to provide AI capabilities:

- Uses gemma3:1b model by default
- Maintains conversation history for each user
- Handles AI prompts through the established secure connection
- Supports context-aware responses

### Setting Up Ollama

1. Install Ollama from [ollama.ai](https://ollama.ai)
2. Pull the required model: `ollama pull gemma3:1b`
3. Ensure Ollama is running when starting ProjectEXO

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
- Client connections are assigned unique UUIDs
- RSA key exchange establishes secure communications
- Authentication verifies user credentials
- Each client is handled in a separate thread
- Encrypted packets are used for all communications
- AI requests are processed and responses returned securely

## Packet Types 📦

The server handles different types of packets:
- Type 1: Regular messages
- Type 9: AI functionality
  - Subtype 1: Text completion/chat

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

This project is licensed under the [GNU General Public License v3 (GPLv3)](LICENSE).
