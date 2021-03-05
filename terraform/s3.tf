# Create S3 bucket with public reading access
resource "aws_s3_bucket" "s3_bucket" {
  bucket        = "aws-external-sorting"
  acl           = "public-read"
  force_destroy = "true"
}

# By default, AWS enables all four options when you create a new S3 bucket via
# the AWS Management Console. However, you need to enable Block Public Access
# explicitly when working with Terraform.
resource "aws_s3_bucket_public_access_block" "s3_bucket_pab" {
  bucket                  = aws_s3_bucket.s3_bucket.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Upload source code to S3 bucket
resource "aws_s3_bucket_object" "source_code_upload" {
  bucket        = aws_s3_bucket.s3_bucket.id
  key           = "external-sorting-1.0.jar"
  source        = "${path.module}/../external-sorting/target/external-sorting-1.0.jar"
  force_destroy = true
}

# Upload source code to S3 bucket
resource "aws_s3_bucket_object" "config_upload" {
  bucket        = aws_s3_bucket.s3_bucket.id
  key           = "application.yml"
  source        = "${path.module}/${var.config_path}"
  force_destroy = true
}

# Create IAM profile role which grants access to S3 buckets
resource "aws_iam_role" "iam_role" {
  name                  = "iam_role_s3_access"
  path                  = "/"
  force_detach_policies = true
  assume_role_policy    = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

# Create IAM instance profile
resource "aws_iam_instance_profile" "instance_profile" {
  name = "instance_profile_s3_access"
  role = aws_iam_role.iam_role.id
}

# Create IAM role policy which grants access to S3 buckets
resource "aws_iam_role_policy" "iam_role_policy" {
  name   = "iam_role_policy"
  role   = aws_iam_role.iam_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": ["arn:aws:s3:::aws-external-sorting", "arn:aws:s3:::aws-external-sorting-input-data"]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": ["arn:aws:s3:::aws-external-sorting/*", "arn:aws:s3:::aws-external-sorting-input-data/*"]
    }
  ]
}
EOF
}