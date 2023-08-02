<template>
  <div>
    <div class="jw-chat">
      <JwChat-index
        ref="jwChat"
        v-model="inputMsg"
        :taleList="taleList"
        :scrollType="scrollType"
        :toolConfig="tool"
        :placeholder="placeholder"
        :config="config"
        :showRightBox="showRightBox"
        :width="width"
        :height="height"
        @enter="bindEnter"
      >
      </JwChat-index>
    </div>
    <div class="ppt">
      <iframe src="https://ow365.cn/?i=31998&ssl=1&furl=https://image-li.oss-cn-hangzhou.aliyuncs.com/math/nine.pptx"
              frameborder="0" allowfullscreen="true" width="100%" height="600px"
      ></iframe>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
      user: {},
      scrollType: 'noroll', // scroll  noroll 俩种类型
      placeholder: '请输入消息...',
      inputMsg: '',
      taleList: [],
      tool: {
        showEmoji: false // 是否显示表情图标
      },
      width: '100%',
      height: '600px',
      config: {
        img: 'https://ts1.cn.mm.bing.net/th/id/R-C.44f7b44e144c3e62a7bf26d3ed9dbc25?rik=4mNHzi4yH%2fL%2bGQ&riu=http%3a%2f%2fwww.kuaipng.com%2fUploads%2fpic%2fw%2f2023%2f02-09%2f135403%2fwater_135403_698_698_.png&ehk=D%2bT8GsjcmjFS9uaNJ2JvSOP8R8kbzcmFHLdm8GV69lU%3d&risl=&pid=ImgRaw&r=0',
        name: 'ChatGPT'
      },
      showRightBox: false,
      flag: true,
      websocket: undefined
    }
  },
  created() {
    this.init()
  },
  methods: {
    /**
     * websocket初始化
     */
    init() {
      this.user = sessionStorage.getItem('user') ? JSON.parse(sessionStorage.getItem('user')) : {}
      if (!this.user) {
        console.log('用户不存在')
      } else {
        let userId = this.user.userId
        console.log('用户id：', userId)
        if (typeof (WebSocket) == 'undefined') {
          console.log('您的浏览器不支持WebSocket')
        } else {
          console.log('您的浏览器支持WebSocket')
          let url = 'ws://localhost:9090/websocket/' + userId
          if (this.websocket != null) {
            this.websocket.close()
            this.websocket = null
          }
          // 开启一个websocket服务
          this.websocket = new WebSocket(url)
          //打开事件
          this.websocket.onopen = function() {
            console.log('websocket已打开')
          }
          //关闭事件
          this.websocket.onclose = function() {
            console.log('websocket已关闭')
          }
          //发生了错误事件
          this.websocket.onerror = function() {
            console.log('websocket发生了错误')
          }
        }
      }
    },

    /**
     * 发送消息
     */
    send() {
      if (!this.inputMsg) {
        this.$message({ message: '请输入内容', type: 'warning' })
      } else {
        if (typeof (WebSocket) == 'undefined') {
          console.log('您的浏览器不支持WebSocket')
        } else {
          console.log('您的浏览器支持WebSocket')
          console.log('发送消息===>', this.inputMsg)

          const userId = this.user.userId
          const text = this.inputMsg
          const mine = true
          const name = this.user.nickName
          const img = this.user.avatar

          //组装消息
          const msgObj = {
            'userId': userId,
            'text': text,
            'mine': mine,
            'name': name,
            'img': img
          }
          this.websocket.send(JSON.stringify(msgObj))
          msgObj.text = { 'text': msgObj.text }
          this.taleList.push(msgObj)
          this.receive()
        }
      }
    },

    /**
     * 接收消息
     */
    receive() {
      this.websocket.onmessage = msg => {
        console.log('收到数据===>', msg.data)
        const data = JSON.parse(msg.data)
        const content = data.text
        data.text = { 'text': content }
        this.taleList.push(data)
      }
    },

    bindEnter(e) {
      console.log(e)
      this.send()
    }
  }
}
</script>

<style>
.jw-chat {
  width: 40%;
  height: 100%;
  position: absolute;
  right: 0;
  bottom: 0;
}

.ppt {
  width: 60%;
  top: 0;
  left: 0;
  bottom: 50px;
}
</style>
