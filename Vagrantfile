Vagrant.configure("2") do |config|
  config.vm.box = "bento/ubuntu-14.04"
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", 2048]
  end

  project = 'Transmart-VCF-Filter'
  path = "/var/www/sites/#{project}"

  config.vm.synced_folder ".", "/vagrant", :disabled => true
  config.vm.synced_folder ".", "/home/vagrant/#{project}", :nfs => true
  config.vm.hostname = "#{project}.dev"

  config.ssh.forward_agent  = true
  config.vm.network :private_network, ip: "10.33.36.150"
  config.vm.provision :shell, inline:"apt-get update -y"
  config.vm.provision :shell, inline:"apt-get update -y"
  config.vm.provision :shell, inline:"apt-get update -y"
  config.vm.provision :shell, inline:"apt-get install -y curl"
  config.vm.provision :shell, inline:"apt-get install -y unzip"
  config.vm.provision :shell, inline:"apt-get install -y zip"
  config.vm.provision :shell, inline:"apt-get install -y openjdk-7-jdk"
  config.vm.provision :shell, inline:"apt-get install -y tomcat7"
  config.vm.provision :shell, path:"installGradle.sh", privileged: false
end
