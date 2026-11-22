"""Notification microservice - entry point with Eureka client registration.

Run:  uvicorn app.main:app --reload --port 8084
  or: python -m app.main
"""
import os
import sys
from pathlib import Path
from contextlib import asynccontextmanager

# Ensure project root is in sys.path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from fastapi import FastAPI
import py_eureka_client.eureka_client as eureka_client

from app.routers import notification

PORT = int(os.getenv("PORT", "8084"))
EUREKA_SERVER = os.getenv("EUREKA_SERVER", "http://localhost:8761/eureka")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: register service as NOTIFICATION in Eureka
    try:
        await eureka_client.init_async(
            eureka_server=EUREKA_SERVER,
            app_name="NOTIFICATION",
            instance_id=f"localhost:notification:{PORT}",
            instance_port=PORT,
            instance_host="localhost",
            status_page_url=f"http://localhost:{PORT}/api/notifications/hello",
        )
        print(f"[Eureka] NOTIFICATION microservice successfully registered on port {PORT}")
    except Exception as e:
        print(f"[Eureka] Warning: Could not register with Eureka: {e}")

    yield

    # Shutdown: deregister service from Eureka
    try:
        await eureka_client.stop_async()
        print("[Eureka] NOTIFICATION microservice deregistered from Eureka")
    except Exception as e:
        print(f"[Eureka] Warning: Error stopping Eureka client: {e}")


app = FastAPI(
    title="Notification Microservice API",
    version="1.0.0",
    description=(
        "Notification microservice (Python / FastAPI, no database). "
        "Registered with Eureka Discovery Server."
    ),
    contact={"name": "Badia Abouhdid"},
    servers=[{"url": f"http://localhost:{PORT}", "description": "Local"}],
    docs_url="/swagger-ui",
    openapi_url="/v3/api-docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

app.include_router(notification.router)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=PORT, reload=False)
