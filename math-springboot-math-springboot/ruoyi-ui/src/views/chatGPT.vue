<template>
  <div class="container">
<!--  <div class="ppt">-->
<!--    <iframe src="https://ow365.cn/?i=31998&ssl=1&furl=https://image-li.oss-cn-hangzhou.aliyuncs.com/math/nine.pptx"-->
<!--            scrolling="no" frameborder="0" allowfullscreen=true width="800" height="450" ></iframe>-->
<!--  </div>-->
  <div class="pythoncode">
    <el-button type="primary" icon="el-icon-circle-check-outline" @click="handleConfirm" round>
      点击保存
    </el-button>
    <el-button icon="el-icon-caret-right" type="info" @click="handleRunCode" round>
      在线运行
    </el-button>
    <CommonEditor>
      language="python"
      @input="changeTextarea"
    </CommonEditor>
  </div>
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
      @clickTalk="talkEvent"
    >
    </JwChat-index>
  </div>
  </div>
</template>

<script>
import CommonEditor from "@/views/CommonEditor";

export default {
  components: {CommonEditor},
  data() {
    return {
      user: {},
      scrollType: 'noroll', // scroll  noroll 俩种类型
      placeholder: '请输入消息...',
      inputMsg: '',
      taleList: [],
      tool: {
        callback: this.toolEvent
      },
      width: '500px',
      height: '650px',
      config: {
        img: 'https://ts1.cn.mm.bing.net/th/id/R-C.44f7b44e144c3e62a7bf26d3ed9dbc25?rik=4mNHzi4yH%2fL%2bGQ&riu=http%3a%2f%2fwww.kuaipng.com%2fUploads%2fpic%2fw%2f2023%2f02-09%2f135403%2fwater_135403_698_698_.png&ehk=D%2bT8GsjcmjFS9uaNJ2JvSOP8R8kbzcmFHLdm8GV69lU%3d&risl=&pid=ImgRaw&r=0',
        name: 'ChatGPT',
        callback: this.bindCover,
        historyConfig: {
          show: true,
          tip: '加载更多提示框,可以直接使用组件的',
          callback: this.bindLoadHistory
        }
      },
      showRightBox: false,
      websocket: undefined,
      pythonContent: {
        pythontext: ''
      }
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
          let url = 'ws://192.168.10.109:9090/websocket/' + userId
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
          const time = new Date()
          const date = time.toLocaleDateString() + ' ' + time.toLocaleTimeString()
          const text = this.inputMsg
          const mine = true
          const id = this.user.userId
          const name = this.user.userName
          const img = this.user.avatar

          //组装消息
          const msgObj = {
            'date': date,
            'text': { 'text': text },
            'mine': mine,
            'name': name,
            'img': img
          }
          this.websocket.send(JSON.stringify(msgObj))
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
        console.log('收到数据===>',msg.data)
        const data = JSON.parse(msg.data)
        this.taleList.push(data)
      }
    },

    /**
     * @description: 点击加载更多的回调函数
     * @param {*}
     * @return {*}
     */
    async bindLoadHistory() {
      const history = new Array(3).fill().map((i, j) => {
        return {

        }
      })
      let list = history.concat(this.taleList)
      this.taleList = list
      console.log('加载历史', list, history)
      //  加载完成后通知组件关闭加载动画
      this.config.historyConfig.tip = '加载完成'
      this.$nextTick(() => {
        this.$refs.jwChat.finishPullDown()
      })
    },

    bindEnter(e) {
      console.log(e)
      this.send()
    },

    toolEvent(type, obj) {
      console.log('tools', type, obj)
    },

    talkEvent(play) {
      console.log(play)
    },
    changeTextarea(val) {
      this.pythonContent.pythontext = val
    },
    handleConfirm() {},
    handleRunCode() {}
  },
  mounted() {

  }
}
</script>

<style>
.container{
  display: flex;
}
.ppt{
  flex: 1;
}
.pythoncode{
  flex: 1;
}
.jw-chat {
  position: absolute;
  right: 0;
  flex: 2;
}

</style>
