# Quick Fix for Order History Service Database Connection

## 🚀 Fastest Solution (Choose One)

### Option 1: Windows Users

```cmd
start-order-history.bat
```

Then run your service:
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Option 2: Linux/Mac Users

```bash
chmod +x start-order-history.sh
./start-order-history.sh
```

Then run your service:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Option 3: Docker Compose (Complete Setup)

```bash
# Start everything
docker-compose -f order-history-docker-compose.yml up -d

# Check logs
docker-compose -f order-history-docker-compose.yml logs -f
```

---

## 🔧 Manual Fix (If Scripts Don't Work)

### Step 1: Start Database Only

```bash
docker run -d \
  --name order-history-postgres \
  -e POSTGRES_DB=order_history_db \
  -e POSTGRES_USER=pgadmin \
  -e POSTGRES_PASSWORD=pgadminpass \
  -p 5433:5432 \
  postgres:15-alpine
```

### Step 2: Set Environment Variables

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5433"
$env:DB_NAME="order_history_db"
$env:DB_USERNAME="pgadmin"
$env:DB_PASSWORD="pgadminpass"
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=5433
export DB_NAME=order_history_db
export DB_USERNAME=pgadmin
export DB_PASSWORD=pgadminpass
```

### Step 3: Update application.yml

Add to `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:order_history_db}
    username: ${DB_USERNAME:pgadmin}
    password: ${DB_PASSWORD:pgadminpass}
```

### Step 4: Run Application

```bash
mvn spring-boot:run
```

---

## ✅ Verification

### 1. Check Database

```bash
# Connect to database
docker exec -it order-history-postgres psql -U pgadmin -d order_history_db

# In psql:
\l                    # List databases
\dt                   # List tables
SELECT 1;            # Test query
\q                   # Exit
```

### 2. Check Application Health

```bash
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 3. Check Application Logs

Look for:
```
HikariPool-1 - Start completed.
Started OrderHistoryServiceApplication in X.XXX seconds
```

---

## 🐛 Common Errors and Fixes

### Error: "Connection refused"

**Cause:** Database not running or wrong port

**Fix:**
```bash
# Check if database is running
docker ps | grep postgres

# Check port
netstat -an | grep 5433

# Restart database
docker restart order-history-postgres
```

### Error: "Authentication failed for user"

**Cause:** Wrong credentials

**Fix:**
```bash
# Stop and remove container
docker stop order-history-postgres
docker rm order-history-postgres

# Start fresh with correct credentials
docker run -d \
  --name order-history-postgres \
  -e POSTGRES_DB=order_history_db \
  -e POSTGRES_USER=pgadmin \
  -e POSTGRES_PASSWORD=pgadminpass \
  -p 5433:5432 \
  postgres:15-alpine
```

### Error: "Database does not exist"

**Cause:** Database not created

**Fix:**
```bash
# Create database
docker exec -it order-history-postgres \
  psql -U pgadmin -c "CREATE DATABASE order_history_db;"
```

### Error: "Port already in use"

**Cause:** Port 5433 occupied

**Fix:**
```bash
# Use different port
docker run -d \
  --name order-history-postgres \
  -e POSTGRES_DB=order_history_db \
  -e POSTGRES_USER=pgadmin \
  -e POSTGRES_PASSWORD=pgadminpass \
  -p 5434:5432 \
  postgres:15-alpine

# Update DB_PORT
export DB_PORT=5434
```

---

## 💡 Pro Tips

1. **Always check database is running first:**
   ```bash
   docker ps | grep postgres
   ```

2. **Test connection before starting app:**
   ```bash
   docker exec order-history-postgres pg_isready
   ```

3. **View real-time logs:**
   ```bash
   docker logs -f order-history-postgres
   ```

4. **Clean start (if all else fails):**
   ```bash
   docker-compose -f order-history-docker-compose.yml down -v
   docker-compose -f order-history-docker-compose.yml up -d
   ```

---

## 📞 Need More Help?

Check these files:
- [DATABASE_CONNECTION_FIX.md](DATABASE_CONNECTION_FIX.md) - Comprehensive guide
- [order-history-docker-compose.yml](order-history-docker-compose.yml) - Docker setup
- [order-history-config/application-docker.yml](order-history-config/application-docker.yml) - Spring config

---

## 🎯 Summary

Your database credentials are:
- **Host:** localhost
- **Port:** 5433
- **Database:** order_history_db
- **Username:** pgadmin
- **Password:** pgadminpass

Make sure these match in:
1. Docker container
2. Environment variables
3. application.yml
