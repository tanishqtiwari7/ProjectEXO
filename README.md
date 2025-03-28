# ProjectEXO - Secure Network Communications Server 🔒

A robust server implementation for encrypted client-server communications with RSA encryption.

## Features ✨

- 🔐 RSA encryption for secure data transmission
- 👥 Multiple client connection support
- 🔑 Key exchange protocol implementation
- 🔒 Secure authentication system
- 📡 Packet-based communication
- 🌐 Client status broadcasting

## System Requirements 🖥️

- Java 21 or higher
- Maven 3.11+ (for building)
- Any operating system that supports Java

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