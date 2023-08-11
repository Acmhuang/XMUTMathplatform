<template>
  <div>
    <!-- 聊天界面 -->
    <div class="jw-chat">
      <!-- 导入聊天组件并传入相应的配置和数据 -->
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
    <!-- PPT、Python展示区域 -->
    <div>
      <keep-alive>
      <component :is="currentView"></component>
      </keep-alive>
      <button  class="button1" @click="changeView('ppt')"><span>PPT</span></button>
      <button  class="button2" @click="changeView('python')"><span>Python</span></button>
    </div>
  </div>
</template>

<script>
import ppt from '../views/ppt/ppt'
import python from '../views/python/python'
export default {
  components: {
    ppt,
    python
  },
  data() {
    return {
      // 用户信息
      user: {},
      // 聊天滚动类型（可选值：'scroll'、'noroll'）
      scrollType: 'noroll',
      // 输入框占位符
      placeholder: '请输入消息...',
      // 用户输入的消息内容
      inputMsg: '',
      // 聊天记录列表
      taleList: [],
      // 聊天工具栏配置
      tool: {
        showEmoji: false, // 是否显示表情图标
      },
      // 聊天窗口宽度
      width: '100%',
      // 聊天窗口高度
      height: '600px',
      // 聊天窗口配置
      config: {
        img: 'https://ts1.cn.mm.bing.net/th/id/R-C.44f7b44e144c3e62a7bf26d3ed9dbc25?rik=4mNHzi4yH%2fL%2bGQ&riu=http%3a%2f%2fwww.kuaipng.com%2fUploads%2fpic%2fw%2f2023%2f02-09%2f135403%2fwater_135403_698_698_.png&ehk=D%2bT8GsjcmjFS9uaNJ2JvSOP8R8kbzcmFHLdm8GV69lU%3d&risl=&pid=ImgRaw&r=0',
        name: 'ChatGPT',
      },
      // 是否显示右侧工具栏
      showRightBox: false,
      flag: true,
      // WebSocket实例
      websocket: undefined,
      //默认显示的页面
      currentView:'ppt'
    };
  },
  created() {
    // 组件创建时初始化WebSocket连接
    this.init();
  },
  methods: {
    /**
     * WebSocket初始化
     */
    init() {
      // 获取用户信息
      this.user = sessionStorage.getItem('user') ? JSON.parse(sessionStorage.getItem('user')) : {};
      if (!this.user) {
        console.log('用户不存在');
      } else {
        let userId = this.user.userId;
        console.log('用户id：', userId);
        // 检查浏览器是否支持WebSocket
        if (typeof WebSocket == 'undefined') {
          console.log('您的浏览器不支持WebSocket');
        } else {
          console.log('您的浏览器支持WebSocket');
          let url = 'ws://localhost:9090/websocket/' + userId;
          if (this.websocket != null) {
            this.websocket.close();
            this.websocket = null;
          }
          // 创建WebSocket连接
          this.websocket = new WebSocket(url);
          // 打开连接事件
          this.websocket.onopen = function() {
            console.log('WebSocket已打开');
          };
          // 关闭连接事件
          this.websocket.onclose = function() {
            console.log('WebSocket已关闭');
          };
          // 错误事件
          this.websocket.onerror = function() {
            console.log('WebSocket发生了错误');
          };
        }
      }
    },

    /**
     * 发送消息
     */
    send() {
      if (!this.inputMsg) {
        this.$message({ message: '请输入内容', type: 'warning' });
      } else {
        if (typeof WebSocket == 'undefined') {
          console.log('您的浏览器不支持WebSocket');
        } else {
          console.log('您的浏览器支持WebSocket');
          console.log('发送消息===>', this.inputMsg);

          const userId = this.user.userId;
          const text = this.inputMsg;
          const mine = true;
          const name = this.user.nickName;
          const img = this.user.avatar;

          // 组装消息对象
          const msgObj = {
            userId: userId,
            text: text,
            mine: mine,
            name: name,
            img: img,
          };
          // 发送消息
          this.websocket.send(JSON.stringify(msgObj));
          // 将消息添加到聊天记录列表
          msgObj.text = { text: msgObj.text };
          this.taleList.push(msgObj);
          // 接收消息
          this.receive();
        }
      }
    },

    /**
     * 接收消息
     */
    receive() {
      this.websocket.onmessage = (msg) => {
        console.log('收到数据===>', msg.data);
        const data = JSON.parse(msg.data);
        const content = data.text;
        data.text = { text: content };
        this.taleList.push(data);
      };
    },

    /**
     * 绑定 Enter 键事件
     */
    bindEnter(e) {
      console.log(e);
      this.send();
    },
    changeView(component) {
      this.currentView = component;
    }
  },
};
</script>

<style>
.jw-chat {
  width: 40%;
  height: 100%;
  position: absolute;
  right: 0;
  bottom: 0;
}

.button1 {
  display: inline-block;
  border-radius: 4px;
  background-color: #f4511e;
  border: none;
  color: #FFFFFF;
  text-align: center;
  font-size: 18px;
  padding: 10px;
  width: 100px;
  transition: all 0.5s;
  cursor: pointer;
  margin: 5px;
  vertical-align:middle;
}

.button1 span {
  cursor: pointer;
  display: inline-block;
  position: relative;
  transition: 0.5s;
}

.button1 span:after {
  content: '»';
  position: absolute;
  opacity: 0;
  top: 0;
  right: -20px;
  transition: 0.5s;
}

.button1:hover span {
  padding-right: 25px;
}

.button1:hover span:after {
  opacity: 1;
  right: 0;
}

.button2 {
  display: inline-block;
  border-radius: 4px;
  background-color: #2775b6;
  border: none;
  color: #FFFFFF;
  text-align: center;
  font-size: 18px;
  padding: 10px;
  width: 100px;
  transition: all 0.5s;
  cursor: pointer;
  margin: 5px;
  vertical-align:middle;
}

.button2 span {
  cursor: pointer;
  display: inline-block;
  position: relative;
  transition: 0.5s;
}

.button2 span:after {
  content: '»';
  position: absolute;
  opacity: 0;
  top: 0;
  right: -20px;
  transition: 0.5s;
}

.button2:hover span {
  padding-right: 25px;
}

.button2:hover span:after {
  opacity: 1;
  right: 0;
}
</style>
