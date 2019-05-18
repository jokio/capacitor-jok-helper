
  Pod::Spec.new do |s|
    s.name = 'CapacitorJokHelper'
    s.version = '0.0.1'
    s.summary = 'Helper functions for jok projects'
    s.license = 'MIT'
    s.homepage = 'https://github.com/jokio/capacitor-jok-helper.git'
    s.author = 'Jok Entertainers'
    s.source = { :git => 'https://github.com/jokio/capacitor-jok-helper.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end