# Add key-pair
resource "aws_key_pair" "key_pair" {
  key_name   = "ec2"
  public_key = file("resources/keypair/ec2.pub")
}

# Create an EC2 instance 1
resource "aws_instance" "instance_1" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.101"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i1_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 1 to EC2 instance 1
resource "aws_volume_attachment" "ebs_attachment_1" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i1_volume.id
  instance_id  = aws_instance.instance_1.id
  force_detach = true
}


# Create an EC2 instance 2
resource "aws_instance" "instance_2" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.102"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i2_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 2 to EC2 instance 2
resource "aws_volume_attachment" "ebs_attachment_2" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i2_volume.id
  instance_id  = aws_instance.instance_2.id
  force_detach = true
}


# Create an EC2 instance 3
resource "aws_instance" "instance_3" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.103"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i3_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 3 to EC2 instance 3
resource "aws_volume_attachment" "ebs_attachment_3" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i3_volume.id
  instance_id  = aws_instance.instance_3.id
  force_detach = true
}


# Create an EC2 instance 4
resource "aws_instance" "instance_4" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.104"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i4_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 4 to EC2 instance 4
resource "aws_volume_attachment" "ebs_attachment_4" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i4_volume.id
  instance_id  = aws_instance.instance_4.id
  force_detach = true
}


# Create an EC2 instance 5
resource "aws_instance" "instance_5" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.105"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i5_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 5 to EC2 instance 5
resource "aws_volume_attachment" "ebs_attachment_5" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i5_volume.id
  instance_id  = aws_instance.instance_5.id
  force_detach = true
}


# Create an EC2 instance 6
resource "aws_instance" "instance_6" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.106"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i6_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 6 to EC2 instance 6
resource "aws_volume_attachment" "ebs_attachment_6" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i6_volume.id
  instance_id  = aws_instance.instance_6.id
  force_detach = true
}


# Create an EC2 instance 7
resource "aws_instance" "instance_7" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.107"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i7_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 7 to EC2 instance 7
resource "aws_volume_attachment" "ebs_attachment_7" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i7_volume.id
  instance_id  = aws_instance.instance_7.id
  force_detach = true
}


# Create an EC2 instance 8
resource "aws_instance" "instance_8" {
  ami                         = var.ec2_image
  instance_type               = var.ec2_type
  availability_zone           = "us-east-1a"
  key_name                    = aws_key_pair.key_pair.key_name
  associate_public_ip_address = "true"
  vpc_security_group_ids      = [aws_security_group.sg_web_in.id, aws_security_group.sg_web_out.id]
  subnet_id                   = aws_subnet.public_subnet.id
  user_data                   = file(var.user_data_file)
  private_ip                  = "192.1.0.108"
  iam_instance_profile        = aws_iam_instance_profile.instance_profile.name
  depends_on                  = ["aws_s3_bucket_object.source_code_upload"]
}

# Create an EBS volume
resource "aws_ebs_volume" "i8_volume" {
  type              = "gp2"
  size              = var.ebs_size
  availability_zone = "us-east-1a"
  encrypted         = "false"
}

# Attach volume 8 to EC2 instance 8
resource "aws_volume_attachment" "ebs_attachment_8" {
  device_name  = "/dev/sdh"
  volume_id    = aws_ebs_volume.i8_volume.id
  instance_id  = aws_instance.instance_8.id
  force_detach = true
}