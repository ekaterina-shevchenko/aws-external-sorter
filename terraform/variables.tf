variable "ebs_size" {
  type    = number
  default = 120
}

variable "ec2_image" {
  type    = string
  default = "ami-047a51fa27710816e"
}

variable "ec2_type" {
  type    = string
  default = "t2.medium"
}

variable "user_data_file" {
  type    = string
  default = "resources/instance_user_data.sh"
}

variable "config_path" {
  type    = string
  default = "../config/application.yml"
}