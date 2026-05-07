# Use the official Python 3.11 slim image (matches the syllabus tech stack)
FROM python:3.11-slim

# Prevent Python from writing .pyc files to disk and ensure terminal output is unbuffered
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Set the working directory inside the container
WORKDIR /app

# Install system dependencies required for building some Python packages (like PyTorch/Chroma)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy the requirements file and install dependencies
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application code into the container
COPY . /app/

# Expose the port the Waitress server runs on
EXPOSE 5000

# Start the Waitress production server
CMD ["python", "app.py"]