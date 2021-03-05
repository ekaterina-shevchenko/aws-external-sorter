# Configure the AWS provider
provider "aws" {
  region  = "us-east-1"
  version = "~> 3.20.0"
}

# Create a VPC
resource "aws_vpc" "vpc" {
  cidr_block           = "192.1.0.0/16"
  enable_dns_hostnames = "true"
}

# Attach IGW to the VPC
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id
}

# Create a public subnet
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "192.1.0.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = "true"
}

# Define a route table
resource "aws_route_table" "route_table" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
}

# Assign the route table to the public subnet
resource "aws_route_table_association" "web_public_rt_association" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.route_table.id
}

# Define security group allowing inbound public access
resource "aws_security_group" "sg_web_in" {
  name        = "web-security-group-all-in"
  description = "Allow access to our webserver"
  vpc_id      = aws_vpc.vpc.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Define security group allowing outbound access to internet
resource "aws_security_group" "sg_web_out" {
  name        = "web-security-group-all-out"
  description = "Allow access to internet"
  vpc_id      = aws_vpc.vpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}