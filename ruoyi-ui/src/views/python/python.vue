<template>
  <div class="python">
    <iframe :src="htmlsrc" allowfullscreen="true" width="100%" height="600px"
    ></iframe>
  </div>
</template>

<script>

export default {
  name: "python",
  data(){
    return {
      htmlsrc:'static/python.html'
    }
  }
};
</script>

<style scoped>
.python {
  width: 60%;
  top: 0;
  left: 0;
  bottom: 50px;
}
</style>
