 # 设置部署到服务器的位置
set :deploy_to, "/home/ewhine/deploy/#{application}"
server 'ewhine@www.weixinwork.com', :app, :web, :db, :primary => true
ssh_options[:port] = 2222
  # roles details
  #  set :password, "@@@"
  # Because we have only a single server, our role declarations look a bit redundant. 
  # An alternative syntax uses the “server” keyword
  # 因为只有一个服务器，所以不使用
  # server 'ewhine@www.gz3rx.com', :app, :web, :db, :primary => true
server 'ewhine@www.weixinwork.com', :app, :web, :db, :primary => true
