<template>
  <!-- 聊天界面模板 -->
  <div style="padding: 10px; height: 100%; position: absolute; top: 0; right: 0;">
    <el-row>
      <el-col :span="20">
        <!-- 聊天消息容器 -->
        <div style="width: 600px; margin: 0 auto; background-color: rgb(52, 53, 64); border-radius: 5px; box-shadow: 0 0 10px #ccc;">
          <!-- 聊天标题 -->
          <div style="text-align: center; line-height: 50px;">
            ChatGPT
          </div>
          <!-- 聊天内容区域 -->
          <div style="height: 400px; overflow: auto; border-top: 1px solid #ccc" v-html="content"></div>
          <!-- 发送消息区域 -->
          <div style="height: 200px">
            <textarea v-model="text" style="height: 150px; width: 100%; padding: 20px; border: none; border-top: 1px solid #ccc; border-bottom: 1px solid #ccc; outline: none; background-color: rgb(64, 65, 79)"></textarea>
            <div style="text-align: right; padding-right: 10px">
              <el-button type="primary" size="mini" @click="send">发送</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import request from '@/utils/request'

let socket

export default {
  name: 'Im',
  data() {
    return {
      circleUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      user: {},
      isCollapse: false,
      users: [],
      text: '',
      messages: [],
      content: '',
      websocket: undefined
    }
  },
  created() {
    this.init()
  },
  methods: {
    // 发送消息方法
    send() {
      if (!this.text) {
        this.$message({ type: 'warning', message: '请输入内容' })
      } else {
        // 检查浏览器是否支持WebSocket
        if (typeof WebSocket == 'undefined') {
          console.log('您的浏览器不支持WebSocket')
        } else {
          console.log('您的浏览器支持WebSocket')
          // 组装待发送的消息 json {"from": "zhang", "to": "admin", "text": "聊天文本"}
          const username = this.user.userName
          let message = { from: username, to: 'ChatGPT', text: this.text }
          console.log('username', username)
          console.log('message', JSON.stringify(message))
          // 将组装好的json发送给服务端，由服务端进行转发
          this.websocket.send(JSON.stringify(message))
          this.messages.push({ user: username, text: this.text })
          // 构建消息内容，本人消息
          this.createContent(null, username, this.text)
          this.text = ''
          this.receive()
        }
      }
    },
    // 接收消息方法
    receive() {
      // 浏览器端收消息，获得从服务端发送过来的文本消息
      this.websocket.onmessage = (msg) => {
        console.log('收到数据====' + msg.data)
        let data = JSON.parse(msg.data)
        this.createContent(data.from, null, data.text)
      }
    },
    // 将收到的消息转换成 HTML
    createContent(remoteUser, nowUser, text) {
      let html
      // 当前用户消息
      if (nowUser) {
        // nowUser 表示是否显示当前用户发送的聊天消息，绿色气泡
        html = '<div class="el-row" style="padding: 5px 0">\n' +
          '  <div class="el-col el-col-22" style="text-align: right; padding-right: 10px">\n' +
          '    <div class="tip left">' + text + '</div>\n' +
          '  </div>\n' +
          '  <div class="el-col el-col-2">\n' +
          '  <span class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">\n' +
          '    <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;">\n' +
          '  </span>\n' +
          '  </div>\n' +
          '</div>'
      } else if (remoteUser) {
        // remoteUser 表示远程用户聊天消息，蓝色的气泡
        html = '<div class="el-row" style="padding: 5px 0">\n' +
          '  <div class="el-col el-col-2" style="text-align: right">\n' +
          '  <span class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">\n' +
          '    <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;">\n' +
          '  </span>\n' +
          '  </div>\n' +
          '  <div class="el-col el-col-22" style="text-align: left; padding-left: 10px">\n' +
          '    <div class="tip right">' + text + '</div>\n' +
          '  </div>\n' +
          '</div>'
      }
      console.log(html)
      this.content += html
    },
    init() {
      // 初始化用户信息
      this.user = sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {}
      let username = this.user.userName
      console.log(username)
      let _this = this
      // 检查浏览器是否支持WebSocket
      if (typeof WebSocket == 'undefined') {
        console.log('您的浏览器不支持WebSocket')
      } else {
        console.log('您的浏览器支持WebSocket')
        // 构建WebSocket连接的URL
        let socketUrl = 'ws://192.168.31.25:9090/websocket/' + username
        if (socket != null) {
          socket.close()
          socket = null
        }
        // 开启一个websocket服务
        this.websocket = new WebSocket(socketUrl)
        this.receive()
        // 打开事件
        this.websocket.onopen = function() {
          console.log('websocket已打开')
        }
        // 关闭事件
        this.websocket.onclose = function() {
          console.log('websocket已关闭')
        }
        // 发生错误事件
        this.websocket.onerror = function() {
          console.log('websocket发生了错误')
        }
      }
    }
  }
}
</script>

<style>
.tip {
  color: rgb(64, 65, 79);
  text-align: center;
  border-radius: 10px;
  font-family: sans-serif;
  padding: 10px;
  width: auto;
  display: inline-block !important;
  display: inline;
}

.right {
  background-color: deepskyblue;
}

.left {
  background-color: forestgreen;
}
</style>
